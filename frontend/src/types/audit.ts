export interface AuditEventItem {
  id: string;
  organizationId: string;
  sequenceNumber: number;
  actorReference: string;
  action: string;
  resourceReference: string;
  eventTimestamp: string;
  metadataJson: string;
  metadataHash: string;
  previousHash: string;
  currentHash: string;
  ingestedAt: string;
}

export interface VerificationRunItem {
  id: string;
  organizationId: string;
  triggerType: string;
  startedAt: string;
  completedAt: string;
  startSequenceNumber: number;
  endSequenceNumber: number;
  totalEventsChecked: number;
  result: 'VERIFIED' | 'FAILED' | 'CHECKPOINT_UNAVAILABLE' | 'PENDING';
  checkpointId?: string;
  failureSequenceNumber?: number;
  failureReason?: string;
}

export interface CheckpointItem {
  id: string;
  organizationId: string;
  sequenceNumber: number;
  checkpointHash: string;
  hashAlgorithm: string;
  storageProvider: 'LOCAL' | 'MINIO' | 'S3';
  storageBucket: string;
  storageObjectKey: string;
  objectSha256: string;
  verificationStatus: string;
  createdAt: string;
}
