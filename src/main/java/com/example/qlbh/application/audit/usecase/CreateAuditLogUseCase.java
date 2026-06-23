package com.example.qlbh.application.audit.usecase;

import com.example.qlbh.application.audit.command.AuditLogCommand;

public interface CreateAuditLogUseCase {

  void execute(AuditLogCommand command);
}