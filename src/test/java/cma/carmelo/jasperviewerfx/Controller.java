package cma.carmelo.jasperviewerfx;

import java.util.Calendar;
import java.util.Date;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import static net.sf.dynamicreports.report.builder.DynamicReports.cht;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.FontBuilder;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;

public class Controller {

    private Stage stage;

    public Controller() {
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void onClickBuildReport(ActionEvent event) {
        System.out.println("Generando reporte...");
        JasperPrint printer = buildPrint();
        System.out.println("Visualizando reporte...");

        JasperFX viewer = new JasperFX(printer);
        viewer.show(800, 600, stage, Modality.WINDOW_MODAL, StageStyle.DECORATED);
    }

    private JasperPrint buildPrint() {
        FontBuilder boldFont = stl.fontArialBold().setFontSize(12);

        TextColumnBuilder<String> seriesColumn = col.column("Series", "series", type.stringType());
        TextColumnBuilder<Date> dateColumn = col.column("Date", "date", type.dateType());
        TextColumnBuilder<Double> highColumn = col.column("High", "high", type.doubleType());
        TextColumnBuilder<Double> lowColumn = col.column("Low", "low", type.doubleType());
        TextColumnBuilder<Double> openColumn = col.column("Open", "open", type.doubleType());
        TextColumnBuilder<Double> closeColumn = col.column("Close", "close", type.doubleType());
        TextColumnBuilder<Double> volumeColumn = col.column("Volume", "volume", type.doubleType());

        try {
            JasperReportBuilder report = report()
                    .setTemplate(Templates.reportTemplate)
                    .columns(seriesColumn, dateColumn, highColumn, lowColumn, openColumn, closeColumn, volumeColumn)
                    .title(Templates.createTitleComponent("Jasper Viewer FX"))
                    .summary(
                            cht.highLowChart()
                            .setTitle("HighLow chart")
                            .setTitleFont(boldFont)
                            .setSeries(seriesColumn)
                            .setDate(dateColumn)
                            .setHigh(highColumn)
                            .setLow(lowColumn)
                            .setOpen(openColumn)
                            .setClose(closeColumn)
                            .setVolume(volumeColumn)
                            .setShowOpenTicks(true)
                            .setShowCloseTicks(true)
                            .setTimeAxisFormat(
                                    cht.axisFormat().setLabel("Date"))
                            .setValueAxisFormat(
                                    cht.axisFormat().setLabel("Value")))
                    .pageFooter(Templates.footerComponent)
                    .setDataSource(createDataSource());
            
            return report.toJasperPrint();
        } catch (DRException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    private JRDataSource createDataSource() {
        DRDataSource dataSource = new DRDataSource("series", "date", "high", "low", "open", "close", "volume");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -20);
        for (int i = 0; i < 50; i++) {
            dataSource.add("serie", c.getTime(), 150 + Math.random() * 50, 20 + Math.random() * 30, 50 + Math.random() * 90, 50 + Math.random() * 110, 50 + Math.random() * 100);
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dataSource;
    }
}
