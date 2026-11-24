import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { RegistroComponent } from './registro/registro.component';
import { MenuComponent } from './menu/menu.component';
import { authGuard } from './auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'registro', component: RegistroComponent },
  { path: 'menu', component: MenuComponent, canActivate: [authGuard] },
  { path: '', redirectTo: 'menu', pathMatch: 'full' },
  { path: '**', redirectTo: 'menu' }
];