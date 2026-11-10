import { Component, ElementRef, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

interface NavLink {
  label: string;
  path: string;
  exact: boolean;
}

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
  host: {
    '(document:click)': 'onDocumentClick($event)',
    '(document:keydown.escape)': 'closeAll()'
  }
})
export class Navbar {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly elementRef = inject(ElementRef<HTMLElement>);

  protected readonly mobileMenuOpen = signal(false);
  protected readonly userMenuOpen = signal(false);

  protected readonly links: NavLink[] = [
    { label: 'Ana Sayfa', path: '/', exact: true },
    { label: 'Oda Tipleri', path: '/rooms', exact: false },
    { label: 'Hakkımızda', path: '/about', exact: false },
    { label: 'Galeri', path: '/gallery', exact: false },
    { label: 'İletişim', path: '/contact', exact: false }
  ];

  toggleMobileMenu(): void {
    this.mobileMenuOpen.update(open => !open);
    this.userMenuOpen.set(false);
  }

  toggleUserMenu(): void {
    this.userMenuOpen.update(open => !open);
  }

  closeAll(): void {
    this.mobileMenuOpen.set(false);
    this.userMenuOpen.set(false);
  }

  logout(): void {
    this.auth.logout();
    this.closeAll();
    this.router.navigate(['/']);
  }

  // Menü dışına tıklanınca menüleri kapat
  onDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target as Node)) {
      this.closeAll();
    }
  }
}