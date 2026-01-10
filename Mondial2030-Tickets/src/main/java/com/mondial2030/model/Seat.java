package com.mondial2030.model;

/**
 * Represents a seat in the stadium.
 */
public class Seat {
    
    private String seatId;
    private int row;
    private int column;
    private SeatStatus status;
    private String section;
    
    public Seat() {
    }
    
    public Seat(String seatId, int row, int column, SeatStatus status) {
        this.seatId = seatId;
        this.row = row;
        this.column = column;
        this.status = status;
    }
    
    public Seat(String seatId, int row, int column, SeatStatus status, String section) {
        this(seatId, row, column, status);
        this.section = section;
    }

    // Getters and Setters
    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "seatId='" + seatId + '\'' +
                ", row=" + row +
                ", column=" + column +
                ", status=" + status +
                ", section='" + section + '\'' +
                '}';
    }
}
