import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { TranslocoModule } from '@ngneat/transloco';
import { CalendarView } from '../../../core/models/calendar-view.type';
import { Employee } from '../../../core/models/employee.model';

@Component({
    selector: 'app-calendar-toolbar',
    imports: [TranslocoModule],
    templateUrl: './calendar-toolbar.component.html',
    styleUrl: './calendar-toolbar.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CalendarToolbarComponent {
    readonly dateLabel = input.required<string>();
    readonly view = input.required<CalendarView>();
    readonly employees = input<Employee[]>([]);
    readonly selectedEmployeeId = input('');

    readonly previous = output<void>();
    readonly next = output<void>();
    readonly today = output<void>();
    readonly viewChange = output<CalendarView>();
    readonly employeeChange = output<string>();
}