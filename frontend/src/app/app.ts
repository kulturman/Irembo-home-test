import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterOutlet, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { AuthService } from './core/auth/auth.service';
import { filter } from 'rxjs';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule, ButtonModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  showHeader = false;
  isAdmin = false;

  ngOnInit(): void {
    this.updateHeaderVisibility();

    // Listen to route changes to update header visibility
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.updateHeaderVisibility();
    });
  }

  updateHeaderVisibility(): void {
    const currentUrl = this.router.url;
    this.showHeader = this.authService.isAuthenticated() && !currentUrl.includes('/login');
    this.isAdmin = this.authService.isAdmin();
  }

  navigateToTemplates(): void {
    this.router.navigate(['/templates']);
  }

  navigateToUsers(): void {
    this.router.navigate(['/users']);
  }

  onLogout(): void {
    this.authService.logout();
  }
}
