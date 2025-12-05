import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { TemplatesComponent } from './features/templates/templates.component';
import { TemplateDetailComponent } from './features/template-detail/template-detail.component';
import { UsersComponent } from './features/users/users.component';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

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
    path: 'templates/:id',
    component: TemplateDetailComponent,
    canActivate: [authGuard]
  },
  {
    path: 'users',
    component: UsersComponent,
    canActivate: [authGuard, adminGuard]
  },
  {
    path: '',
    redirectTo: '/templates',
    pathMatch: 'full'
  }
];
