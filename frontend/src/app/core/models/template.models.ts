export interface CreateTemplateRequest {
  name: string;
  content: string;
}

export interface UpdateTemplateRequest {
  name: string;
  content: string;
}

export interface TemplateResponse {
  id: string;
  name: string;
  content: string;
  variables: string;
  createdAt: string;
  updatedAt: string;
}

export interface ResourceId {
  id: string;
}

export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalPages: number;
  totalElements: number;
  last: boolean;
  first: boolean;
  empty: boolean;
}

export interface GenerateCertificateRequest {
  templateId: string;
  variables: { [key: string]: string };
}

export enum CertificateStatus {
  QUEUED = 'QUEUED',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}

export interface CertificateResponse {
  id: string;
  templateId: string;
  templateName: string;
  variables: string;
  status: CertificateStatus;
  downloadToken: string;
  createdAt: string;
  updatedAt: string;
}
