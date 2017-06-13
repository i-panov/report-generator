public class Main {

    public static void main(String[] args) {
        try {
            ReportService reportService = new ReportService(args[0], args[1], args[2]);
            reportService.read();
            reportService.write();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
