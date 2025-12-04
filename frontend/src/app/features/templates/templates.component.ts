import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-templates',
  standalone: true,
  imports: [CardModule, ButtonModule],
  template: `
    <div class="min-h-screen bg-gray-50 p-8">
      <div class="max-w-7xl mx-auto">
        <div class="flex justify-between items-center mb-8">
          <h1 class="text-3xl font-bold text-gray-900">Certificate Templates</h1>
          <p-button
            label="Logout"
            icon="pi pi-sign-out"
            (onClick)="onLogout()"
            severity="secondary"
          />
        </div>

        <p-card>
          <p class="text-gray-600">Templates management coming soon...</p>
        </p-card>
      </div>
    </div>
  `
})
export class TemplatesComponent {
  private readonly authService = inject(AuthService);

  onLogout(): void {
    this.authService.logout();
  }
}
