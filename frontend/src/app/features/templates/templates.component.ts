import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { FloatLabelModule } from 'primeng/floatlabel';
import { EditorModule } from 'primeng/editor';
import { DataViewModule } from 'primeng/dataview';
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
    DataViewModule,
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

  templates: TemplateResponse[] = [];
  loading = false;
  error: string | null = null;

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
    this.loading = true;
    this.error = null;

    this.templateService.getTemplates().subscribe({
      next: (data) => {
        this.templates = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load templates. Please try again.';
        this.loading = false;
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

  onLogout(): void {
    this.authService.logout();
  }

  get name() {
    return this.createTemplateForm.get('name');
  }

  get content() {
    return this.createTemplateForm.get('content');
  }
}
