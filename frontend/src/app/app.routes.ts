import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { TemplatesComponent } from './features/templates/templates.component';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'templates',
    component: TemplatesComponent,
    canActivate: [authGuard]
  },
  {
    path: '',
    redirectTo: '/templates',
    pathMatch: 'full'
  }
];
