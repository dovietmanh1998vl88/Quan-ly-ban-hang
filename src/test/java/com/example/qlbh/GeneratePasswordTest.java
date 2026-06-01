package com.example.qlbh;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePasswordTest {

  @Test
  void generateHash() {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    String hash = encoder.encode("Admin@123456");
    System.out.println("Hash: " + hash);

    // Verify luôn — đảm bảo hash đúng
    boolean matches = encoder.matches("Admin@123456", hash);
    System.out.println("Verify: " + matches); // phải là true
  }

}
