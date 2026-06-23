package com.example.qlbh.common.response;

import java.util.List;
import org.springframework.data.domain.Page;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PageResponse<T> {

  private List<T> items;
  private int page;
  private int size;
  private long total;

  public PageResponse(
      List<T> items,
      int page,
      int size,
      long total
  ) {
    this.items = items;
    this.page = page;
    this.size = size;
    this.total = total;
  }

  // Getter (nếu cần)
  public List<T> getItems() {
    return items;
  }

  public int getPage() {
    return page;
  }

  public int getSize() {
    return size;
  }

  public long getTotal() {
    return total;
  }

  // ===== FIX CHUẨN =====
  public static <T> PageResponse<T> from(Page<T> page) {
    return new PageResponse<>(
        page.getContent(),      // items
        page.getNumber(),       // page
        page.getSize(),         // size
        page.getTotalElements() // total
    );
  }
}
