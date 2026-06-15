package com.example.qlbh.presentation.oder.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record OrderRevenueReportRequest(
    @NotNull(message = "Từ ngày không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate tungay,
    @NotNull(message = "Đến ngày không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate denngay
) {


}
