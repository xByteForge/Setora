package com.byteforge.setorabackend.appointment.dtos;

public interface AppointmentsTodaySummaryProjection {
    Long getTotal();
    Long getConfirmed();
    Long getPending();
    Long getCancelled();
}
