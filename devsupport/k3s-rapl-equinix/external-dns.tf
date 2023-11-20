#################################
# IAM                           #
#################################

resource "aws_iam_user" "external_dns" {
  name = "${var.subdomain}-external-dns-controller"
  path = "/${var.subdomain}/"
}

resource "aws_iam_access_key" "external_dns" {
  user = aws_iam_user.external_dns.name
}

resource "aws_iam_user_policy" "external_dns" {
  name   = "${var.subdomain}-external-dns-controller"
  user   = aws_iam_user.external_dns.name
  policy = data.aws_iam_policy_document.external_dns.json
}

data "aws_iam_policy_document" "external_dns" {
  statement {
    effect    = "Allow"
    actions   = ["route53:ChangeResourceRecordSets"]
    resources = ["arn:aws:route53:::hostedzone/${aws_route53_zone.public.id}"]
  }

  statement {
    effect  = "Allow"
    actions = [
      "route53:ListHostedZones",
      "route53:ListResourceRecordSets",
      "route53:ListTagsForResource"
    ]
    resources = ["*"]
  }
}


#################################
# Helm                          #
#################################

locals {
  external_dns_manifest_files = [
    {
      path    = "/var/lib/rancher/k3s/server/manifests/external-dns.yaml"
      content = <<-EOT
apiVersion: helm.cattle.io/v1
kind: HelmChart
metadata:
  name: external-dns-controller
  namespace: kube-system
spec:
  repo: https://kubernetes-sigs.github.io/external-dns
  chart: external-dns
  version: 1.13.1
  targetNamespace: kube-system
  valuesContent: |-
    fullnameOverride: external-dns-controller
    nameOverride: external-dns-controller
    priorityClassName: "system-cluster-critical"
    provider: aws
    policy: sync
    txtOwnerId: ${var.subdomain}
    domainFilters:
      - ${local.fqdn}
    env:
      - name: AWS_DEFAULT_REGION
        value: ${var.aws_region}
      - name: AWS_SHARED_CREDENTIALS_FILE
        value: /.aws/credentials
    secretConfiguration:
      enabled: true
      mountPath: /.aws
      data:
        credentials: |
          [default]
          aws_access_key_id = ${aws_iam_access_key.external_dns.id}
          aws_secret_access_key = ${aws_iam_access_key.external_dns.secret}
    podAnnotations:
      prometheus.io/port: "7979"
      prometheus.io/scrape: "true"
    resources:
      limits:
        cpu: 75m
        memory: 92Mi
      requests:
        cpu: 50m
        memory: 64Mi
EOT
    }
  ]
}