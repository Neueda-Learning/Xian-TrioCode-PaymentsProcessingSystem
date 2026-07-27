package org.hsbc.triocodebackend.common.result;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Generic pagination result container used as the {@code data} payload
 * inside {@link Result} for list/page endpoints.
 *
 * <p>Usage example:
 * <pre>
 *   PageResult&lt;PaymentListItemVO&gt; page = PageResult.of(records, total, pageNum, pageSize);
 *   return Result.ok(page);
 * </pre>
 *
 * @param <T> element type of the page records
 */
@Data
@NoArgsConstructor
public class PageResult<T> {

    /** Records on the current page */
    private List<T> records;

    /** Total number of matching records across all pages */
    private long total;

    /** Current page number (1-based) */
    private int pageNum;

    /** Number of records per page */
    private int pageSize;

    /** Total number of pages */
    private int totalPages;

    // ----------------------------------------------------------------
    // Private full constructor
    // ----------------------------------------------------------------

    private PageResult(List<T> records, long total, int pageNum, int pageSize) {
        this.records = records == null ? Collections.emptyList() : records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalPages = (pageSize > 0) ? (int) Math.ceil((double) total / pageSize) : 0;
    }

    // ----------------------------------------------------------------
    // Static factory methods
    // ----------------------------------------------------------------

    /**
     * Build a {@link PageResult} from raw pagination data.
     *
     * @param records  current-page records (maybe null → treated as empty list)
     * @param total    total matched records count
     * @param pageNum  current page number (1-based)
     * @param pageSize page size
     * @param <T>      element type
     * @return populated {@link PageResult}
     */
    public static <T> PageResult<T> of(List<T> records, long total, int pageNum, int pageSize) {
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    /**
     * Convenience factory when the caller already has a full list and wants
     * to wrap it as a single-page result (e.g. for non-paginated endpoints
     * that still return list data via the common structure).
     *
     * @param records all records
     * @param <T>     element type
     * @return {@link PageResult} with pageNum=1, pageSize=records.size()
     */
    public static <T> PageResult<T> ofAll(List<T> records) {
        int size = records == null ? 0 : records.size();
        return new PageResult<>(records, size, 1, size == 0 ? 1 : size);
    }

    /**
     * Return an empty page result (e.g. when the query returns no rows).
     *
     * @param pageNum  requested page number
     * @param pageSize requested page size
     * @param <T>      element type
     * @return {@link PageResult} with zero total and empty records
     */
    public static <T> PageResult<T> empty(int pageNum, int pageSize) {
        return new PageResult<>(Collections.<T>emptyList(), 0L, pageNum, pageSize);
    }
}

