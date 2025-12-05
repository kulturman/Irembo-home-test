import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { SelectButtonModule } from 'primeng/selectbutton';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { UserService, PageResponse } from '../../core/services/user.service';
import { UserResponse, CreateUserRequest, UserRole } from '../../core/models/user.models';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    SelectButtonModule,
    ToastModule
  ],
  providers: [MessageService],
  templateUrl: './users.component.html',
  styleUrl: './users.component.css'
})
export class UsersComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly fb = inject(FormBuilder);
  private readonly messageService = inject(MessageService);

  users: UserResponse[] = [];
  totalRecords = 0;
  loading = false;
  displayDialog = false;

  userForm!: FormGroup;

  roleOptions = [
    { label: 'User', value: UserRole.USER },
    { label: 'Admin', value: UserRole.ADMIN }
  ];

  ngOnInit(): void {
    this.initForm();
    this.loadUsers();
  }

  initForm(): void {
    this.userForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      name: ['', Validators.required],
      role: [UserRole.USER, Validators.required]
    });
  }

  loadUsers(page: number = 0, size: number = 10): void {
    this.loading = true;
    this.userService.getAllUsers(page, size).subscribe({
      next: (response: PageResponse<UserResponse>) => {
        this.users = response.content;
        this.totalRecords = response.totalElements;
        this.loading = false;
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load users'
        });
        this.loading = false;
      }
    });
  }

  onPageChange(event: any): void {
    this.loadUsers(event.page, event.rows);
  }

  showCreateDialog(): void {
    this.userForm.reset({ role: UserRole.USER });
    this.displayDialog = true;
  }

  hideDialog(): void {
    this.displayDialog = false;
    this.userForm.reset();
  }

  createUser(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    const request: CreateUserRequest = this.userForm.value;

    this.userService.createUser(request).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'User created successfully'
        });
        this.hideDialog();
        this.loadUsers();
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error.error?.detail || 'Failed to create user'
        });
      }
    });
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
