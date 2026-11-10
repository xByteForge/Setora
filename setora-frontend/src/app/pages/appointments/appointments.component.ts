import { CommonModule } from "@angular/common";
import { Component } from "@angular/core";
import { TranslocoModule } from "@ngneat/transloco";
import { SidebarComponent } from "../../core/layout/sidebar/sidebar.component";

@Component({
  imports: [CommonModule, TranslocoModule, SidebarComponent],
  selector: "app-appointments.component",
  styleUrl: "./appointments.component.css",
  templateUrl: "./appointments.component.html",
})
export class AppointmentsComponent {}
