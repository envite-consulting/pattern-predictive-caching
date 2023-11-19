#################################
# Pool                          #
#################################

locals {
  cognito_admin_group_name = "admin"
  cognito_user_group_name = "user"
}

resource "aws_cognito_user_pool" "main" {
  name = var.subdomain

  alias_attributes = ["email", "preferred_username"]

  username_configuration {
    case_sensitive = false
  }

  auto_verified_attributes = ["email"]

  lambda_config {
    custom_message = aws_lambda_function.cognito_email_domain_verify.arn
    post_confirmation = aws_lambda_function.cognito_add_user_to_group.arn
  }
}

resource "aws_cognito_user_pool_domain" "main" {
  domain       = replace(local.fqdn, ".", "-")
  user_pool_id = aws_cognito_user_pool.main.id
}

resource "aws_cognito_user_group" "admin" {
  name         = local.cognito_admin_group_name
  user_pool_id = aws_cognito_user_pool.main.id
  description  = "Admin group managed by Terraform"
  precedence   = 1
}

resource "aws_cognito_user_group" "user" {
  name         = local.cognito_user_group_name
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
    name = "lambda_basic_execution"
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
    effect = "Allow"
    actions = ["logs:CreateLogGroup"]
    resources = ["arn:aws:logs:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:*"]
  }
  statement {
    effect = "Allow"
    actions = ["logs:CreateLogStream", "logs:PutLogEvents"]
    resources = ["arn:aws:logs:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:log-group:/aws/lambda/${local.lambda_cognito_email_domain_verify_name}:*"]
  }
}

resource "aws_lambda_function" "cognito_email_domain_verify" {
  filename      = data.archive_file.lambda_cognito_email_domain_verify.output_path
  function_name = local.lambda_cognito_email_domain_verify_name
  role          = aws_iam_role.lambda_cognito_email_domain_verify.arn
  handler       = "index.handler"

  source_code_hash = data.archive_file.lambda_cognito_email_domain_verify.output_base64sha256

  runtime = "nodejs20.x"
}

data "archive_file" "lambda_cognito_email_domain_verify" {
  type        = "zip"
  output_path = "${path.module}/target/lambda/${local.lambda_cognito_email_domain_verify_name}.zip"

  source {
    filename = "index.js"
    content  = <<-EOT
exports.handler = function(event, context) {
  // Log the event information for debugging purposes.
  console.log('Received event:', JSON.stringify(event, null, 2));
  if (event.request.userAttributes.email.endsWith("@${var.admin_email_domain}") || event.request.userAttributes.email.endsWith("@${var.auto_verify_email_domain}")) {
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
    name = "lambda_basic_execution"
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
    effect = "Allow"
    actions = ["logs:CreateLogGroup"]
    resources = ["arn:aws:logs:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:*"]
  }
  statement {
    effect = "Allow"
    actions = ["logs:CreateLogStream", "logs:PutLogEvents"]
    resources = ["arn:aws:logs:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:log-group:/aws/lambda/${local.lambda_cognito_add_user_to_group_name}:*"]
  }
}

resource "aws_iam_policy" "cognito_add_user_to_group" {
  name        = local.lambda_cognito_add_user_to_group_name
  policy      = data.aws_iam_policy_document.cognito_add_user_to_group.json
}

data "aws_iam_policy_document" "cognito_add_user_to_group" {
  statement {
    effect = "Allow"
    actions = ["cognito-idp:AdminAddUserToGroup"]
    resources = [aws_cognito_user_pool.main.arn]
  }
}

resource "aws_iam_policy_attachment" "cognito_add_user_to_group" {
  name       = local.lambda_cognito_add_user_to_group_name
  roles      = [aws_iam_role.lambda_cognito_add_user_to_group.name]
  policy_arn = aws_iam_policy.cognito_add_user_to_group.arn
}

resource "aws_lambda_function" "cognito_add_user_to_group" {
  filename      = data.archive_file.lambda_cognito_add_user_to_group.output_path
  function_name = local.lambda_cognito_add_user_to_group_name
  role          = aws_iam_role.lambda_cognito_add_user_to_group.arn
  handler       = "index.handler"

  source_code_hash = data.archive_file.lambda_cognito_add_user_to_group.output_base64sha256

  runtime = "nodejs20.x"
}

data "archive_file" "lambda_cognito_add_user_to_group" {
  type        = "zip"
  output_path = "${path.module}/target/lambda/${local.lambda_cognito_add_user_to_group_name}.zip"

  source {
    filename = "index.ts"
    content  = <<-EOT
import { Callback, Context, PostConfirmationTriggerEvent } from "aws-lambda";
import AWS from "aws-sdk";

export async function main(event: PostConfirmationTriggerEvent, _context: Context, callback: Callback): Promise<void> {
  const { userPoolId, userName } = event;

  var groupName:string = "${local.cognito_user_group_name}";
  if (event.request.userAttributes.email && event.request.userAttributes.email.endsWith("@${var.admin_email_domain}")) {
    groupName = "${local.cognito_admin_group_name}"
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
}

export function addUserToGroup({
  userPoolId,
  username,
  groupName,
}: {
  userPoolId: string;
  username: string;
  groupName: string;
}): Promise<{
  $response: AWS.Response<Record<string, string>, AWS.AWSError>;
}> {
  const params = {
    GroupName: groupName,
    UserPoolId: userPoolId,
    Username: username,
  };

  const cognitoIdp = new AWS.CognitoIdentityServiceProvider();
  return cognitoIdp.addUserToGroup(params).promise();
}
EOT
  }
}
