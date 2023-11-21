host_enabled = true
hostname     = "k3s"
metro        = "sk"
plan         = "m3.small.x86"
terminate_in = "48h"

k3s_channel = "latest"

host_users_gh = ["ueisele", "nadjahagen"]

aws_region               = "eu-north-1"
route53_public_main_zone = "codelabs.dev"
subdomain                = "lecture"
acme_email               = "code@uweeisele.eu"

auto_verify_domains = ["envite.de", "hft-stuttgart.de"]
admin_domains       = ["envite.de"]
