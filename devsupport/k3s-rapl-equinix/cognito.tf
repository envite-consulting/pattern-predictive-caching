#################################
# Pool                          #
#################################

resource "aws_cognito_user_pool" "main" {
  name = var.subdomain

  username_attributes = ["email"]

  username_configuration {
    case_sensitive = false
  }

  auto_verified_attributes = ["email"]

  lambda_config {
    custom_message    = aws_lambda_function.cognito_email_domain_verify.arn
    post_confirmation = aws_lambda_function.cognito_add_user_to_group.arn
  }
}

resource "aws_cognito_user_pool_domain" "main" {
  domain       = replace(local.fqdn, ".", "-")
  user_pool_id = aws_cognito_user_pool.main.id
}

resource "aws_cognito_user_group" "admin" {
  name         = var.admin_group_name
  user_pool_id = aws_cognito_user_pool.main.id
  description  = "Admin group managed by Terraform"
  precedence   = 1
}

resource "aws_cognito_user_group" "user" {
  name         = var.user_group_name
  user_pool_id = aws_cognito_user_pool.main.id
  description  = "User group managed by Terraform"
  precedence   = 2
}


#################################
# Verification                  #
#################################

locals {
  lambda_cognito_email_domain_verify_name = "cognito_email_domain_verify"
}

resource "aws_iam_role" "lambda_cognito_email_domain_verify" {
  name               = "lambda_${local.lambda_cognito_email_domain_verify_name}"
  assume_role_policy = data.aws_iam_policy_document.lambda_cognito_email_domain_verify_assume_role.json

  inline_policy {
    name   = "lambda_basic_execution"
    policy = data.aws_iam_policy_document.lambda_cognito_email_domain_verify_basic_execution.json
  }
}

data "aws_iam_policy_document" "lambda_cognito_email_domain_verify_assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

data "aws_iam_policy_document" "lambda_cognito_email_domain_verify_basic_execution" {
  statement {
    effect    = "Allow"
    actions   = ["logs:CreateLogGroup"]
    resources = ["arn:aws:logs:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:*"]
  }
  statement {
    effect    = "Allow"
    actions   = ["logs:CreateLogStream", "logs:PutLogEvents"]
    resources = [
      "arn:aws:logs:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:log-group:/aws/lambda/${local.lambda_cognito_email_domain_verify_name}:*"
    ]
  }
}

resource "aws_lambda_permission" "cognito_email_domain_verify" {
  statement_id  = "AllowExecutionFromCognito"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.cognito_email_domain_verify.function_name
  principal     = "cognito-idp.amazonaws.com"
  source_arn    = aws_cognito_user_pool.main.arn
}

resource "aws_lambda_function" "cognito_email_domain_verify" {
  filename      = data.archive_file.lambda_cognito_email_domain_verify.output_path
  function_name = local.lambda_cognito_email_domain_verify_name
  role          = aws_iam_role.lambda_cognito_email_domain_verify.arn
  handler       = "index.handler"

  source_code_hash = data.archive_file.lambda_cognito_email_domain_verify.output_base64sha256

  runtime = "nodejs20.x"
}

# https://docs.aws.amazon.com/cognito/latest/developerguide/cognito-user-identity-pools-working-with-aws-lambda-triggers.html
data "archive_file" "lambda_cognito_email_domain_verify" {
  type        = "zip"
  output_path = "${path.module}/target/lambda/${local.lambda_cognito_email_domain_verify_name}.zip"

  source {
    filename = "index.js"
    content  = <<-EOT
exports.handler = function(event, context) {
  console.log('Received event:', JSON.stringify(event, null, 2));

  let auto_verify = false;
  for (const [index, domain] of ${jsonencode(var.auto_verify_domains)}.entries()) {
    if (event.request.userAttributes.email.endsWith("@" + domain)) {
      auto_verify = true;
    }
  }

  if (auto_verify) {
    console.log ("This is an approved email address. Proceeding to send verification email.");
    event.response.emailSubject = "Signup Verification Code";
    event.response.emailMessage = "Thank you for signing up. " + event.request.codeParameter + " is your verification code.";
    context.done(null, event);
  } else {
    console.log ("This is not an approved email address. Throwing error.");
    var error = new Error('EMAIL_DOMAIN_ERR');
    context.done(error, event);
 }
};
EOT
  }
}


#################################
# Group Assignment              #
#################################

locals {
  lambda_cognito_add_user_to_group_name = "cognito_add_user_to_group"
}

resource "aws_iam_role" "lambda_cognito_add_user_to_group" {
  name               = "lambda_${local.lambda_cognito_add_user_to_group_name}"
  assume_role_policy = data.aws_iam_policy_document.lambda_cognito_add_user_to_group_assume_role.json

  inline_policy {
    name   = "lambda_basic_execution"
    policy = data.aws_iam_policy_document.lambda_cognito_add_user_to_group_basic_execution.json
  }
}

data "aws_iam_policy_document" "lambda_cognito_add_user_to_group_assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

data "aws_iam_policy_document" "lambda_cognito_add_user_to_group_basic_execution" {
  statement {
    effect    = "Allow"
    actions   = ["logs:CreateLogGroup"]
    resources = ["arn:aws:logs:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:*"]
  }
  statement {
    effect    = "Allow"
    actions   = ["logs:CreateLogStream", "logs:PutLogEvents"]
    resources = [
      "arn:aws:logs:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:log-group:/aws/lambda/${local.lambda_cognito_add_user_to_group_name}:*"
    ]
  }
}

resource "aws_iam_policy" "cognito_add_user_to_group" {
  name   = local.lambda_cognito_add_user_to_group_name
  policy = data.aws_iam_policy_document.cognito_add_user_to_group.json
}

data "aws_iam_policy_document" "cognito_add_user_to_group" {
  statement {
    effect    = "Allow"
    actions   = ["cognito-idp:AdminAddUserToGroup"]
    resources = [aws_cognito_user_pool.main.arn]
  }
}

resource "aws_iam_policy_attachment" "cognito_add_user_to_group" {
  name       = local.lambda_cognito_add_user_to_group_name
  roles      = [aws_iam_role.lambda_cognito_add_user_to_group.name]
  policy_arn = aws_iam_policy.cognito_add_user_to_group.arn
}

resource "aws_lambda_permission" "cognito_add_user_to_group" {
  statement_id  = "AllowExecutionFromCognito"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.cognito_add_user_to_group.function_name
  principal     = "cognito-idp.amazonaws.com"
  source_arn    = aws_cognito_user_pool.main.arn
}

resource "aws_lambda_function" "cognito_add_user_to_group" {
  filename      = data.archive_file.lambda_cognito_add_user_to_group.output_path
  function_name = local.lambda_cognito_add_user_to_group_name
  role          = aws_iam_role.lambda_cognito_add_user_to_group.arn
  handler       = "index.handler"

  source_code_hash = data.archive_file.lambda_cognito_add_user_to_group.output_base64sha256

  runtime = "nodejs16.x"
}

# https://docs.aws.amazon.com/cognito/latest/developerguide/cognito-user-identity-pools-working-with-aws-lambda-triggers.html
data "archive_file" "lambda_cognito_add_user_to_group" {
  type        = "zip"
  output_path = "${path.module}/target/lambda/${local.lambda_cognito_add_user_to_group_name}.zip"

  source {
    filename = "index.js"
    content  = <<-EOT
const AWS = require("aws-sdk");

exports.handler = async (event, _context, callback) => {
  const { userPoolId, userName } = event;

  let groupName = "${var.user_group_name}";
  for (const [index, domain] of ${jsonencode(var.admin_domains)}.entries()) {
    if (event.request.userAttributes.email.endsWith("@" + domain)) {
      groupName = "${var.admin_group_name}";
    }
  }

  try {
    await addUserToGroup({
      userPoolId,
      username: userName,
      groupName: groupName,
    });
    return callback(null, event);
  } catch (error) {
    return callback(error, event);
  }
};

function addUserToGroup({ userPoolId, username, groupName }) {
  const params = {
    GroupName: groupName,
    UserPoolId: userPoolId,
    Username: username,
  };
  const cognitoIdp = new AWS.CognitoIdentityServiceProvider();
  return cognitoIdp.adminAddUserToGroup(params).promise();
}
EOT
  }
}
