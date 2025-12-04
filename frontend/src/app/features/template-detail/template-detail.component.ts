import { Component, inject, OnInit, signal } from '@angular/core';
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

  template = signal<TemplateResponse | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  certificates = signal<CertificateResponse[]>([]);
  certificatesLoading = signal(false);
  certificatesError = signal<string | null>(null);
  downloadingCertificateId = signal<string | null>(null);
  copiedCertificateId = signal<string | null>(null);

  certificateForm: FormGroup;
  generatingCertificate = signal(false);

  showEditDialog = signal(false);
  editTemplateForm: FormGroup;
  editLoading = signal(false);
  editErrorMessage = signal('');

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
    this.loading.set(true);
    this.error.set(null);

    this.templateService.getTemplate(id).subscribe({
      next: (data) => {
        this.template.set(data);
        this.loading.set(false);
        this.buildCertificateForm(data);
        this.loadCertificates(id);
      },
      error: () => {
        this.error.set('Failed to load template. Please try again.');
        this.loading.set(false);
      }
    });
  }

  loadCertificates(templateId: string): void {
    this.certificatesLoading.set(true);
    this.certificatesError.set(null);

    this.certificateService.getCertificatesByTemplate(templateId).subscribe({
      next: (response) => {
        this.certificates.set(response.content);
        this.certificatesLoading.set(false);
      },
      error: () => {
        this.certificatesError.set('Failed to load certificates.');
        this.certificatesLoading.set(false);
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

    const tmpl = this.template();
    if (!tmpl) return;

    this.generatingCertificate.set(true);

    const request: GenerateCertificateRequest = {
      templateId: tmpl.id,
      variables: this.certificateForm.value
    };

    this.certificateService.generateCertificate(request).subscribe({
      next: (response) => {
        this.generatingCertificate.set(false);
        this.certificateForm.reset();
        alert(`Certificate generation started! Certificate ID: ${response.id}`);
        // Reload certificates list
        this.loadCertificates(tmpl.id);
      },
      error: () => {
        this.generatingCertificate.set(false);
        alert('Failed to generate certificate. Please try again.');
      }
    });
  }

  onEdit(): void {
    const tmpl = this.template();
    if (tmpl) {
      this.showEditDialog.set(true);
      this.editErrorMessage.set('');

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

    const tmpl = this.template();
    if (!tmpl) return;

    this.editLoading.set(true);
    this.editErrorMessage.set('');

    this.templateService.updateTemplate(tmpl.id, this.editTemplateForm.value).subscribe({
      next: () => {
        this.editLoading.set(false);
        this.showEditDialog.set(false);
        this.loadTemplate(tmpl.id);
      },
      error: () => {
        this.editLoading.set(false);
        this.editErrorMessage.set('Failed to update template. Please try again.');
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
    const tmpl = this.template();
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
    this.downloadingCertificateId.set(certificate.id);

    this.certificateService.downloadCertificate(certificate.downloadToken).subscribe({
      next: (blob) => {
        // Create a download link and trigger download
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `certificate-${certificate.id}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.downloadingCertificateId.set(null);
      },
      error: () => {
        this.downloadingCertificateId.set(null);
        alert('Failed to download certificate. Please try again.');
      }
    });
  }

  onCopyCertificateLink(certificate: CertificateResponse): void {
    const downloadUrl = `${window.location.origin}/api/public/certificates/download/${certificate.downloadToken}`;

    navigator.clipboard.writeText(downloadUrl).then(() => {
      this.copiedCertificateId.set(certificate.id);

      // Reset the copied state after 2 seconds
      setTimeout(() => {
        this.copiedCertificateId.set(null);
      }, 2000);
    }).catch(() => {
      alert('Failed to copy link to clipboard.');
    });
  }
}
