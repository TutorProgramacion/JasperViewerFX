package cma.carmelo.jasperviewerfx;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.PrintPageFormat;
import net.sf.jasperreports.engine.export.JRGraphics2DExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleGraphics2DExporterOutput;
import net.sf.jasperreports.export.SimpleGraphics2DReportConfiguration;

/**
 * Created by Carmelo Marín Abrego on 8/9/15.
 */
public class JasperFX {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private final int RESOLUTION;
    private final JasperPrint print;
    private final Canvas canvas;
    private final ComboBox<Integer> cbxZoom;
    private final ScrollBar sbHorizontal;
    private final ScrollBar sbVertical;
    private final JasperReportsContext jasperReportsContext;

    private JRGraphics2DExporter exporter;
    private Page pages;
    private boolean loaded;
    private Stage stage;

    private AnimationTimer timer;
    private long lastTimerCall;

    public JasperFX(JasperPrint print) {
        this.print = print;
        this.canvas = new Canvas();
        this.cbxZoom = new ComboBox<>();
        this.sbHorizontal = new ScrollBar();
        this.sbVertical = new ScrollBar();
        this.loaded = false;
        this.jasperReportsContext = DefaultJasperReportsContext.getInstance();
        
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        RESOLUTION = toolkit.getScreenResolution();
    }

    public void show() {
        this.show(WIDTH, HEIGHT, null, Modality.NONE, StageStyle.DECORATED);
    }

    public void show(int width, int height, StageStyle style) {
        this.show(width, height, null, Modality.NONE, style);
    }

    public void show(int width, int height, final Window owner, Modality modality, StageStyle style) {
        pages = new Page(print.getPages().size() - 1, 0, 0);
        Scene scene = createScene(pages, width, height);

        stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Jasper ViewerFX");
        stage.setOnShown(s -> showFirstPage());
        stage.initOwner(owner);
        stage.initModality(modality);
        stage.initStyle(style);
        stage.sizeToScene();
        stage.show();
    }

    private Scene createScene(Page page, int width, int height) {
        VBox vbxRoot = new VBox();

        Region topShadow = new Region();
        topShadow.setStyle("-fx-background-color: linear-gradient(from 100% 0% to 100% 100%, gray, transparent);");
        topShadow.setMaxSize(Double.MAX_VALUE, 5);

        cbxZoom.setMaxWidth(100);
        cbxZoom.setEditable(true);
        cbxZoom.setItems(FXCollections.observableArrayList(50, 75, 100, 125, 150, 175, 200, 250, 400, 800));
        cbxZoom.getSelectionModel().select(2);
        cbxZoom.getSelectionModel().selectedItemProperty().addListener((p, o, z) -> showPage(page.getActual()));
        cbxZoom.setConverter(new ZoomConverter());
        cbxZoom.getEditor().setContextMenu(new ContextMenu());
        cbxZoom.getEditor().setOnKeyTyped(k -> {
            if (!Character.isDigit(k.getCharacter().charAt(0))) {
                k.consume();
            }
        });

        sbVertical.setOrientation(Orientation.VERTICAL);

        sbVertical.heightProperty().addListener((p, o, n) -> handleResize());
        sbHorizontal.widthProperty().addListener((p, o, n) -> handleResize());

        sbHorizontal.valueProperty().addListener((p, o, n) -> handleResize() /*renderPage(page.getActual())*/);
        sbVertical.valueProperty().addListener((p, o, n) -> handleResize() /*renderPage(page.getActual())*/);

        GridPane scrollPane = new GridPane();
        scrollPane.add(sbHorizontal, 0, 1);
        scrollPane.add(sbVertical, 1, 0);
        scrollPane.add(canvas, 0, 0);
        scrollPane.add(topShadow, 0, 0);

        GridPane.setHgrow(sbVertical, Priority.NEVER);
        GridPane.setVgrow(sbHorizontal, Priority.NEVER);

        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        cc.setMinWidth(0.0);

        scrollPane.getColumnConstraints().add(cc);

        RowConstraints rc = new RowConstraints();
        rc.setValignment(VPos.TOP);
        rc.setVgrow(Priority.ALWAYS);
        rc.setMinHeight(0.0);

        scrollPane.getRowConstraints().add(rc);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        canvas.heightProperty().bind(sbVertical.heightProperty());
        canvas.widthProperty().bind(sbHorizontal.widthProperty());

        Button btnNext = new Button("Next", new FontAwesomeIconView(FontAwesomeIcon.FORWARD));
        Button btnPrev = new Button("Prev", new FontAwesomeIconView(FontAwesomeIcon.BACKWARD));
        Button btnLast = new Button("Last", new FontAwesomeIconView(FontAwesomeIcon.STEP_FORWARD));
        Button btnFirst = new Button("First", new FontAwesomeIconView(FontAwesomeIcon.STEP_BACKWARD));
        Button btnSave = new Button("Save", new MaterialDesignIconView(MaterialDesignIcon.FLOPPY));
        Button btnPrint = new Button("Print", new MaterialIconView(MaterialIcon.LOCAL_PRINTSHOP));

        btnPrint.setOnAction(ae -> printReport());
        btnSave.setOnAction(ae -> saveReport());

        btnNext.setOnAction((ae) -> showPage(nextPage()));
        btnPrev.setOnAction((ae) -> showPage(prevPage()));
        btnLast.setOnAction((ae) -> showPage(lastPage()));
        btnFirst.setOnAction((ae) -> showPage(firstPage()));

        btnFirst.getStyleClass().add("left-pill");
        btnNext.getStyleClass().add("center-pill");
        btnLast.getStyleClass().add("right-pill");
        btnPrev.getStyleClass().add("center-pill");

        btnFirst.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnNext.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnLast.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnPrev.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnSave.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnPrint.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        
        btnPrint.setTooltip(new Tooltip("Imprimir"));
        btnSave.setTooltip(new Tooltip("Guardar"));

        btnFirst.disableProperty().bind(page.firstPageProperty());
        btnPrev.disableProperty().bind(page.firstPageProperty());
        btnLast.disableProperty().bind(page.lastPageProperty());
        btnNext.disableProperty().bind(page.lastPageProperty());

        Text pageInfo = new Text("0 / 0");
        pageInfo.textProperty().bind(page.actualPageProperty()
                .asString().concat(" / ").concat(page.getLast() + 1));

        HBox navBar = new HBox(btnFirst, btnPrev, btnNext, btnLast);
        navBar.setAlignment(Pos.CENTER);

        ToolBar hbxToolBar = new ToolBar(btnSave, btnPrint, new Separator(),
                navBar, new Separator(), pageInfo, new Separator(), cbxZoom);

        vbxRoot.getChildren().addAll(hbxToolBar, scrollPane);

        return new Scene(vbxRoot, width, height);
    }

    private void saveReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("Reporte.pdf");
        fileChooser.setTitle("Guardar Reporte");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Document", Arrays.asList("*.pdf", "*.PDF")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DOCX Document", Arrays.asList("*.docx", "*.DOCX")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XLSX Document", Arrays.asList("*.xlsx", "*.XLSX")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Document", Arrays.asList("*.html", "*.HTML")));

        File file = fileChooser.showSaveDialog(stage);

        if (fileChooser.getSelectedExtensionFilter() != null && fileChooser.getSelectedExtensionFilter().getExtensions() != null) {
            java.util.List<String> selectedExtension = fileChooser.getSelectedExtensionFilter().getExtensions();
            try {
                if (selectedExtension.contains("*.pdf")) {
                    JasperExportManager.exportReportToPdfFile(print, file.getAbsolutePath());
                } else if (selectedExtension.contains("*.html")) {
                    JasperExportManager.exportReportToHtmlFile(print, file.getAbsolutePath());
                } else if (selectedExtension.contains("*.docx")) {
                    ReportExporter.docx(print, file.getAbsolutePath());
                } else if (selectedExtension.contains("*.xlsx")) {
                    ReportExporter.xlsx(print, file.getAbsolutePath());
                }
            } catch (JRException e) {
                e.printStackTrace();
            }
        }
    }

    private void printReport() {
        try {
            JasperPrintManager.printReport(print, true);
        } catch (JRException e) {
            e.printStackTrace();
        }
    }

    private int nextPage() {
        return pages.moveNext().getActual();
    }

    private int prevPage() {
        return pages.movePrev().getActual();
    }

    private int lastPage() {
        return pages.moveLast().getActual();
    }

    private int firstPage() {
        return pages.moveFirst().getActual();
    }

    private float getZoom() {
        Integer zoom = cbxZoom.getSelectionModel().getSelectedItem();
        return (zoom / 100.0f) * RESOLUTION / 72.0f;
    }

    private void showFirstPage() {
        updateScrollSize(print, 0, getZoom());
        renderPage(0);
        loaded = true;
    }

    private void showPage(int page) {
        sbVertical.setValue(0);
        sbHorizontal.setValue(0);

        updateScrollSize(print, page, getZoom());
        renderPage(page);
    }

    private void handleResize() {
        if (loaded) {
            lastTimerCall = System.nanoTime();
            if (timer == null) {
                timer = new AnimationTimer() {
                    @Override
                    public void handle(long now) {
                        if (now > lastTimerCall + 2_500_000l /*30_000_000l*/) {
                            updateScrollSize(print, pages.getActual(), getZoom());
                            renderPage(pages.getActual());
                            timer.stop();
                        }
                    }
                };
            }
            timer.start();
        }
    }

    private void updateScrollSize(JasperPrint printer, int page, float zoom) {
        PrintPageFormat pageFormat = printer.getPageFormat(page);

        int image_width = (int) (pageFormat.getPageWidth() * zoom);
        int image_height = (int) (pageFormat.getPageHeight() * zoom);

        int canvas_height = (int) canvas.getHeight();
        int canvas_width = (int) canvas.getWidth();

        double maxv = image_height - canvas_height;
        double visv = maxv - (maxv * (maxv / canvas_height));

        sbVertical.setMax(maxv);
        sbVertical.setVisibleAmount(visv);

        double maxh = image_width - canvas_width;
        double vish = maxh - (maxh * (maxh / canvas_width));

        sbHorizontal.setMax(maxh);
        sbHorizontal.setVisibleAmount(vish);
    }

    private void renderPage(int actual) {
        try {
            float zoomRatio = getZoom();

            PrintPageFormat pageFormat = print.getPageFormat(actual);

            int image_width = (int) (pageFormat.getPageWidth() * zoomRatio);
            int image_height = (int) (pageFormat.getPageHeight() * zoomRatio);
            int ch = (int) canvas.getHeight(), cw = (int) canvas.getWidth();
            int xpos = cw > image_width ? (cw / 2) - (image_width / 2) : 0;
            int ypos = ch > image_height ? (ch / 2) - (image_height / 2) : 0;

            BufferedImage bimage = getImage(print, actual, zoomRatio);
            WritableImage wimage = SwingFXUtils.toFXImage(bimage, null);

            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, cw, ch);
            gc.drawImage(wimage, xpos, ypos);

        } catch (JRException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage getImage(JasperPrint jasperPrint, int pageIndex, float zoom) throws JRException {
        int canvas_width = (int) canvas.getWidth();
        int canvas_height = (int) canvas.getHeight();

        BufferedImage pageImage = new BufferedImage(canvas_width, canvas_height, BufferedImage.TYPE_INT_ARGB);

        if (exporter == null) {
            exporter = new JRGraphics2DExporter(jasperReportsContext);
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        }

        int offsetx = (int) sbHorizontal.getValue();
        int offsety = (int) sbVertical.getValue();

        SimpleGraphics2DExporterOutput output = new SimpleGraphics2DExporterOutput();
        output.setGraphics2D((Graphics2D) pageImage.getGraphics());
        output.getGraphics2D().translate(-offsetx, -offsety);

        exporter.setExporterOutput(output);

        SimpleGraphics2DReportConfiguration configuration = new SimpleGraphics2DReportConfiguration();
        configuration.setPageIndex(pageIndex);
        configuration.setZoomRatio(zoom);

        exporter.setConfiguration(configuration);
        exporter.exportReport();

        return pageImage;
    }

}
