#################################
# Common                        #
#################################

locals {
  fqdn            = "${var.subdomain}.${var.route53_public_main_zone}"
  jupyterhub_fqdn = "jupyterhub.${local.fqdn}"
}

data "aws_region" "current" {}

#################################
# Cognito                       #
#################################

data "aws_cognito_user_pools" "selected" {
  name = var.subdomain
}

resource "aws_cognito_user_pool_client" "main" {
  name         = replace(local.jupyterhub_fqdn, ".", "-")
  user_pool_id = data.aws_cognito_user_pools.selected.ids[0]

  supported_identity_providers         = ["COGNITO"]
  callback_urls                        = ["https://${local.jupyterhub_fqdn}/hub/oauth_callback"]
  logout_urls                          = ["https://${local.jupyterhub_fqdn}/"]
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["code"]
  allowed_oauth_scopes                 = ["openid"]
  explicit_auth_flows                  = [
    "ALLOW_ADMIN_USER_PASSWORD_AUTH",
    "ALLOW_CUSTOM_AUTH",
    "ALLOW_USER_PASSWORD_AUTH",
    "ALLOW_USER_SRP_AUTH",
    "ALLOW_REFRESH_TOKEN_AUTH"
  ]
  generate_secret = true
}


#################################
# Helm                          #
#################################

data "template_file" "jupyterhub_values" {
  template = file("${path.module}/values.yaml")
  vars     = {
    jupyterhub_fqdn               = local.jupyterhub_fqdn
    jupyterhub_logout_url_encoded = urlencode("https://${local.jupyterhub_fqdn}/")
    cognito_app_id                = aws_cognito_user_pool_client.main.id
    cognito_app_secret            = aws_cognito_user_pool_client.main.client_secret
    cognito_domain                = "${replace(local.fqdn, ".", "-")}.auth.${data.aws_region.current.name}.amazoncognito.com"
    allow_all_domains             = var.allow_all_domains
    allowed_domains               = jsonencode(var.allowed_domains)
    admin_domains                 = jsonencode(var.admin_domains)
  }
}

resource "helm_release" "jupyterhub" {
  name       = "jupyterhub"
  namespace  = "jupyterhub"
  repository = "https://hub.jupyter.org/helm-chart/"
  chart      = "jupyterhub"
  version    = var.jupyterhub_chart_version

  create_namespace = true

  values = [
    data.template_file.jupyterhub_values.rendered
  ]
}
