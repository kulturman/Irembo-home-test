import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { FloatLabelModule } from 'primeng/floatlabel';
import { EditorModule } from 'primeng/editor';
import { SkeletonModule } from 'primeng/skeleton';
import { TagModule } from 'primeng/tag';
import { AuthService } from '../../core/auth/auth.service';
import { TemplateService } from '../../core/services/template.service';
import { TemplateResponse } from '../../core/models/template.models';

@Component({
  selector: 'app-templates',
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
    ReactiveFormsModule
  ],
  templateUrl: './templates.component.html'
})
export class TemplatesComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly templateService = inject(TemplateService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  templates = signal<TemplateResponse[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);

  showCreateDialog = false;
  createTemplateForm: FormGroup;
  createLoading = false;
  createErrorMessage = '';

  constructor() {
    this.createTemplateForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      content: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.loadTemplates();
  }

  loadTemplates(): void {
    this.loading.set(true);
    this.error.set(null);

    this.templateService.getTemplates().subscribe({
      next: (data) => {
        this.templates.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load templates. Please try again.');
        this.loading.set(false);
      }
    });
  }

  openCreateDialog(): void {
    this.showCreateDialog = true;
    this.createTemplateForm.reset();
    this.createErrorMessage = '';
  }

  onCreateTemplate(): void {
    if (this.createTemplateForm.invalid) {
      this.createTemplateForm.markAllAsTouched();
      return;
    }

    this.createLoading = true;
    this.createErrorMessage = '';

    this.templateService.createTemplate(this.createTemplateForm.value).subscribe({
      next: () => {
        this.createLoading = false;
        this.showCreateDialog = false;
        this.createTemplateForm.reset();
        this.loadTemplates();
      },
      error: () => {
        this.createLoading = false;
        this.createErrorMessage = 'Failed to create template. Please try again.';
      }
    });
  }

  getContentPreview(html: string, maxLength: number = 150): string {
    const tmp = document.createElement('DIV');
    tmp.innerHTML = html;
    const text = tmp.textContent || tmp.innerText || '';
    return text.length > maxLength
      ? text.substring(0, maxLength) + '...'
      : text;
  }

  getVariablesArray(variables: string): string[] {
    try {
      return JSON.parse(variables);
    } catch {
      return [];
    }
  }

  onViewTemplate(id: string): void {
    this.router.navigate(['/templates', id]);
  }

  get name() {
    return this.createTemplateForm.get('name');
  }

  get content() {
    return this.createTemplateForm.get('content');
  }
}
