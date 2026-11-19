import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { TranslocoModule } from '@ngneat/transloco';
import { UserService } from '../../core/services/user.service';
import { ProfileDetailsComponent } from './profile-details/profile-details.component';
import { AccountSettingsComponent } from './account-settings/account-settings.component';


export type SettingsTab = 'profile' | 'settings';

@Component({
  selector: 'app-settings',
  imports: [TranslocoModule, ProfileDetailsComponent, AccountSettingsComponent],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SettingsComponent {
  private readonly userService = inject(UserService);

  readonly user = this.userService.currentUser;
  readonly activeTab = signal<SettingsTab>('profile');

  

  selectTab(tab: SettingsTab): void {
    this.activeTab.set(tab);
  }
}