import { ChangeDetectionStrategy, Component, inject, computed } from '@angular/core';
import { TranslocoModule } from '@ngneat/transloco';
import { UserService } from '../../core/services/user.service';
import { ProfileDetailsComponent } from './profile-details/profile-details.component';
import { AccountSettingsComponent } from './account-settings/account-settings.component';
import { ActivatedRoute, Router } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';

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

  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private readonly tabParam = toSignal(
    this.route.queryParamMap.pipe(map((params) => params.get('tab'))),
    { initialValue: null },
  );

  readonly activeTab = computed<SettingsTab>(() =>
    this.tabParam() === 'settings' ? 'settings' : 'profile',
  );

  selectTab(tab: SettingsTab): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { tab },
      replaceUrl: true,
    });
  }
}
