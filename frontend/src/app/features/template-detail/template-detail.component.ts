import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { FloatLabelModule } from 'primeng/floatlabel';
import { EditorModule } from 'primeng/editor';
import { SkeletonModule } from 'primeng/skeleton';
import { TagModule } from 'primeng/tag';
import { TableModule } from 'primeng/table';
import { TemplateService } from '../../core/services/template.service';
import { CertificateService } from '../../core/services/certificate.service';
import { TemplateResponse, GenerateCertificateRequest, CertificateResponse, CertificateStatus } from '../../core/models/template.models';

@Component({
  selector: 'app-template-detail',
  standalone: true,
  imports: [
    CommonModule,
    CardModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    FloatLabelModule,
    EditorModule,
    SkeletonModule,
    TagModule,
    TableModule,
    ReactiveFormsModule
  ],
  templateUrl: './template-detail.component.html'
})
export class TemplateDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly templateService = inject(TemplateService);
  private readonly certificateService = inject(CertificateService);
  private readonly fb = inject(FormBuilder);

  template: TemplateResponse | null = null;
  loading = true;
  error: string | null = null;

  certificates: CertificateResponse[] = [];
  certificatesLoading = false;
  certificatesError: string | null = null;
  downloadingCertificateId: string | null = null;
  copiedCertificateId: string | null = null;

  certificateForm: FormGroup;
  generatingCertificate = false;

  showEditDialog = false;
  editTemplateForm: FormGroup;
  editLoading = false;
  editErrorMessage = '';

  constructor() {
    this.certificateForm = this.fb.group({});
    this.editTemplateForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      content: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    const templateId = this.route.snapshot.paramMap.get('id');
    if (templateId) {
      this.loadTemplate(templateId);
    }
  }

  loadTemplate(id: string): void {
    this.loading = true;
    this.error = null;

    this.templateService.getTemplate(id).subscribe({
      next: (data) => {
        this.template = data;
        this.loading = false;
        this.buildCertificateForm(data);
        this.loadCertificates(id);
      },
      error: () => {
        this.error = 'Failed to load template. Please try again.';
        this.loading = false;
      }
    });
  }

  loadCertificates(templateId: string): void {
    this.certificatesLoading = true;
    this.certificatesError = null;

    this.certificateService.getCertificatesByTemplate(templateId).subscribe({
      next: (response) => {
        this.certificates = response.content;
        this.certificatesLoading = false;
      },
      error: () => {
        this.certificatesError = 'Failed to load certificates.';
        this.certificatesLoading = false;
      }
    });
  }

  buildCertificateForm(template: TemplateResponse): void {
    const variables = this.getVariablesArray(template.variables);
    const formGroup: any = {};

    variables.forEach(variable => {
      formGroup[variable] = ['', Validators.required];
    });

    this.certificateForm = this.fb.group(formGroup);
  }

  getVariablesArray(variables: string): string[] {
    try {
      return JSON.parse(variables);
    } catch {
      return [];
    }
  }

  onGenerateCertificate(): void {
    if (this.certificateForm.invalid) {
      this.certificateForm.markAllAsTouched();
      return;
    }

    const tmpl = this.template;
    if (!tmpl) return;

    this.generatingCertificate = true;

    const request: GenerateCertificateRequest = {
      templateId: tmpl.id,
      variables: this.certificateForm.value
    };

    this.certificateService.generateCertificate(request).subscribe({
      next: (response) => {
        this.generatingCertificate = false;
        this.certificateForm.reset();
        alert(`Certificate generation started! Certificate ID: ${response.id}`);
        // Reload certificates list
        this.loadCertificates(tmpl.id);
      },
      error: () => {
        this.generatingCertificate = false;
        alert('Failed to generate certificate. Please try again.');
      }
    });
  }

  onEdit(): void {
    const tmpl = this.template;
    if (tmpl) {
      this.showEditDialog = true;
      this.editErrorMessage = '';

      setTimeout(() => {
        this.editTemplateForm.patchValue({
          name: tmpl.name,
          content: tmpl.content
        });
      }, 100);
    }
  }

  onSaveEdit(): void {
    if (this.editTemplateForm.invalid) {
      this.editTemplateForm.markAllAsTouched();
      return;
    }

    const tmpl = this.template;
    if (!tmpl) return;

    this.editLoading = true;
    this.editErrorMessage = '';

    this.templateService.updateTemplate(tmpl.id, this.editTemplateForm.value).subscribe({
      next: () => {
        this.editLoading = false;
        this.showEditDialog = false;
        this.loadTemplate(tmpl.id);
      },
      error: () => {
        this.editLoading = false;
        this.editErrorMessage = 'Failed to update template. Please try again.';
      }
    });
  }

  onBack(): void {
    this.router.navigate(['/templates']);
  }

  get editName() {
    return this.editTemplateForm.get('name');
  }

  get editContent() {
    return this.editTemplateForm.get('content');
  }

  getStatusSeverity(status: CertificateStatus): 'success' | 'secondary' | 'info' | 'warn' | 'danger' | 'contrast' {
    switch (status) {
      case CertificateStatus.COMPLETED:
        return 'success';
      case CertificateStatus.PROCESSING:
        return 'info';
      case CertificateStatus.QUEUED:
        return 'secondary';
      case CertificateStatus.FAILED:
        return 'danger';
      default:
        return 'contrast';
    }
  }

  onReloadCertificates(): void {
    const tmpl = this.template;
    if (tmpl) {
      this.loadCertificates(tmpl.id);
    }
  }

  getVariablesFromJson(variables: string): Array<{key: string, value: string}> {
    try {
      return JSON.parse(variables);
    } catch {
      return [];
    }
  }

  onDownloadCertificate(certificate: CertificateResponse): void {
    this.downloadingCertificateId = certificate.id;

    this.certificateService.downloadCertificate(certificate.downloadToken).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `certificate-${certificate.id}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.downloadingCertificateId = null;
      },
      error: () => {
        this.downloadingCertificateId = null;
        alert('Failed to download certificate. Please try again.');
      }
    });
  }

  onCopyCertificateLink(certificate: CertificateResponse): void {
    const downloadUrl = `${window.location.origin}/api/public/certificates/download/${certificate.downloadToken}`;

    navigator.clipboard.writeText(downloadUrl).then(() => {
      this.copiedCertificateId = certificate.id;

      // Reset the copied state after 2 seconds
      setTimeout(() => {
        this.copiedCertificateId = null;
      }, 2000);
    }).catch(() => {
      alert('Failed to copy link to clipboard.');
    });
  }
}
