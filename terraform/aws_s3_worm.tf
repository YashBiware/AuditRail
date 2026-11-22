terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.40"
    }
  }
}

variable "aws_region" {
  type        = string
  default     = "us-east-1"
  description = "Target AWS deployment region"
}

variable "environment" {
  type        = string
  default     = "production"
  description = "Environment tier (production, staging, demo)"
}

variable "retention_period_days" {
  type        = number
  default     = 365
  description = "WORM retention window in days (COMPLIANCE mode)"
}

provider "aws" {
  region = var.aws_region
}

# -----------------------------------------------------------------------------
# AWS KMS Customer Managed Key (CMK) for Envelope Encryption
# -----------------------------------------------------------------------------
resource "aws_kms_key" "auditrail_kms_key" {
  description             = "AuditRail Checkpoint WORM Storage Encryption Key"
  deletion_window_in_days = 30
  enable_key_rotation     = true

  tags = {
    Project     = "AuditRail"
    Environment = var.environment
    Compliance  = "DPDP-Act-2023-WORM"
  }
}

resource "aws_kms_alias" "auditrail_kms_alias" {
  name          = "alias/auditrail-${var.environment}-key"
  target_key_id = aws_kms_key.auditrail_kms_key.key_id
}

# -----------------------------------------------------------------------------
# S3 Immutable WORM Bucket with Object Lock (COMPLIANCE Mode)
# -----------------------------------------------------------------------------
resource "aws_s3_bucket" "auditrail_worm_bucket" {
  bucket        = "auditrail-checkpoints-${var.environment}"
  force_destroy = false # NEVER allow accidental deletion of audit trail

  # Object Lock MUST be enabled at bucket creation time
  object_lock_enabled = true

  tags = {
    Project     = "AuditRail"
    Environment = var.environment
    Compliance  = "WORM-Compliance"
  }
}

# Enforce Versioning (Prerequisite for S3 Object Lock)
resource "aws_s3_bucket_versioning" "worm_versioning" {
  bucket = aws_s3_bucket.auditrail_worm_bucket.id
  versioning_configuration {
    status = "Enabled"
  }
}

# Enforce Server-Side KMS Encryption
resource "aws_s3_bucket_server_side_encryption_configuration" "worm_encryption" {
  bucket = aws_s3_bucket.auditrail_worm_bucket.id

  rule {
    apply_server_side_encryption_by_default {
      kms_master_key_id = aws_kms_key.auditrail_kms_key.arn
      sse_algorithm     = "aws:kms"
    }
    bucket_key_enabled = true
  }
}

# Enforce Public Access Block
resource "aws_s3_bucket_public_access_block" "worm_public_block" {
  bucket = aws_s3_bucket.auditrail_worm_bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Default WORM Object Lock Configuration (Compliance Mode)
resource "aws_s3_bucket_object_lock_configuration" "worm_config" {
  bucket = aws_s3_bucket.auditrail_worm_bucket.id

  rule {
    default_retention {
      mode = "COMPLIANCE"
      days = var.retention_period_days
    }
  }

  depends_on = [aws_s3_bucket_versioning.worm_versioning]
}

# -----------------------------------------------------------------------------
# IAM Role and Least-Privilege Policy for Backend Application Service
# -----------------------------------------------------------------------------
resource "aws_iam_policy" "auditrail_app_s3_policy" {
  name        = "AuditRailAppS3WormPolicy-${var.environment}"
  description = "Allows AuditRail backend to write WORM objects and fetch checkpoints. Denies all delete/overwrite actions."

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid      = "AllowListAndRead"
        Effect   = "Allow"
        Action   = [
          "s3:ListBucket",
          "s3:GetBucketLocation",
          "s3:GetObject",
          "s3:GetObjectVersion",
          "s3:GetObjectRetention",
          "s3:GetObjectLegalHold"
        ]
        Resource = [
          aws_s3_bucket.auditrail_worm_bucket.arn,
          "${aws_s3_bucket.auditrail_worm_bucket.arn}/*"
        ]
      },
      {
        Sid      = "AllowWriteWithLock"
        Effect   = "Allow"
        Action   = [
          "s3:PutObject",
          "s3:PutObjectRetention",
          "s3:PutObjectLegalHold"
        ]
        Resource = "${aws_s3_bucket.auditrail_worm_bucket.arn}/*"
      },
      {
        Sid      = "ExplicitDenyDeleteActions"
        Effect   = "Deny"
        Action   = [
          "s3:DeleteObject",
          "s3:DeleteObjectVersion",
          "s3:DeleteBucketPolicy",
          "s3:PutLifecycleConfiguration"
        ]
        Resource = [
          aws_s3_bucket.auditrail_worm_bucket.arn,
          "${aws_s3_bucket.auditrail_worm_bucket.arn}/*"
        ]
      },
      {
        Sid      = "AllowKMSKeyUsage"
        Effect   = "Allow"
        Action   = [
          "kms:Encrypt",
          "kms:Decrypt",
          "kms:GenerateDataKey*"
        ]
        Resource = aws_kms_key.auditrail_kms_key.arn
      }
    ]
  })
}

output "s3_bucket_name" {
  value       = aws_s3_bucket.auditrail_worm_bucket.id
  description = "Name of the provisioned WORM S3 bucket"
}

output "kms_key_arn" {
  value       = aws_kms_key.auditrail_kms_key.arn
  description = "ARN of the Customer Managed KMS Key"
}

output "app_iam_policy_arn" {
  value       = aws_iam_policy.auditrail_app_s3_policy.arn
  description = "IAM Policy ARN to attach to backend EC2/EKS role"
}
