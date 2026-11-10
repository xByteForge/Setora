import { CommonModule } from "@angular/common";
import { Component } from "@angular/core";
import { TranslocoModule } from "@ngneat/transloco";

@Component({
  imports: [CommonModule, TranslocoModule],
  selector: "app-appointments.component",
  styleUrl: "./appointments.component.css",
  templateUrl: "./appointments.component.html",
})
export class AppointmentsComponent {}
