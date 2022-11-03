package ru.ifmo.java.server_architectures_testing.application.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import ru.ifmo.java.server_architectures_testing.ServerArchitectureType;
import ru.ifmo.java.server_architectures_testing.application.logic.TestParam;
import ru.ifmo.java.server_architectures_testing.application.logic.TestResult;
import ru.ifmo.java.server_architectures_testing.application.logic.TestsRunner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

public class ApplicationGui extends JFrame {

    private static final @NotNull String APP_TITLE = "Server architectures testing application";
    private final @NotNull JTextField xTextField = new JTextField("10");
    private final @NotNull JTextField nTextField = new JTextField("500");
    private final @NotNull JTextField mTextField = new JTextField("5");
    private final @NotNull JTextField deltaTextField = new JTextField("0");
    private final @NotNull JTextField paramStartValTextField = new JTextField("10");
    private final @NotNull JTextField paramEndValTextField = new JTextField("1000");
    private final @NotNull JTextField paramStepTextField = new JTextField("200");
    private final @NotNull JTextField resultsFolderTextField = new JTextField(System.getProperty("user.dir"));
    private final @NotNull JTextField serverHostTextField = new JTextField("localhost");
    private final @NotNull ButtonGroup architectureTypeSelector = new ButtonGroup();
    private final @NotNull ButtonGroup testParamSelector = new ButtonGroup();
    private final @NotNull ChartPanel requestAverageTimeChart;
    private final @NotNull ChartPanel clientProcessTimeChart;
    private final @NotNull ChartPanel taskExecutionTimeChart;

    public ApplicationGui() {
        super(APP_TITLE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setBounds(0, 0, screenSize.width, screenSize.height);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        XYSeries series = new XYSeries("");
        XYDataset xyDataset = new XYSeriesCollection(series);
        requestAverageTimeChart = new ChartPanel(createChart(ChartTitles.requestAverageTime, TestParam.N.toString(), xyDataset));
        clientProcessTimeChart = new ChartPanel(createChart(ChartTitles.clientProcessTime, TestParam.N.toString(), xyDataset));
        taskExecutionTimeChart = new ChartPanel(createChart(ChartTitles.taskExecutionTime, TestParam.N.toString(), xyDataset));

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2, 0, 0));

        initGraphPanels(panel);
        initControlPanel(panel);

        this.getContentPane().add(panel);
    }

    public static void main(String[] args) {
        ApplicationGui app = new ApplicationGui();
        app.setVisible(true);
    }

    private ServerArchitectureType getArchitectureTypeParam() {
        ButtonModel buttonModel = architectureTypeSelector.getSelection();
        try {
            return ServerArchitectureType.valueOf(buttonModel.getActionCommand());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private TestParam getTestParam() {
        ButtonModel buttonModel = testParamSelector.getSelection();
        try {
            return TestParam.valueOf(buttonModel.getActionCommand());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private int getXParam() {
        return getIntParamFromString(xTextField.getText());
    }

    private int getNParam() {
        return getIntParamFromString(nTextField.getText());
    }

    private int getMParam() {
        return getIntParamFromString(mTextField.getText());
    }

    private int getDeltaParam() {
        return getIntParamFromString(deltaTextField.getText());
    }

    private int getStartValueParam() {
        return getIntParamFromString(paramStartValTextField.getText());
    }

    private int getEndValueParam() {
        return getIntParamFromString(paramEndValTextField.getText());
    }

    private int getStepParam() {
        return getIntParamFromString(paramStepTextField.getText());
    }

    private @Nullable File getResultsFolder() {
        File dir = new File(resultsFolderTextField.getText());
        if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }
        return dir;
    }

    private @NotNull String getServerHost() {
        return serverHostTextField.getText();
    }

    private int getIntParamFromString(@NotNull String maybeNumber) {
        try {
            return Integer.parseInt(maybeNumber);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void initControlPanel(@NotNull JPanel panel) {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());

        initResultsFolderPanel(controlPanel);
        initServerHostPanel(controlPanel);
        initArchitecturesTypeChoiceButtons(controlPanel);
        initTestParameter(controlPanel);
        initParameterChangingFields(controlPanel);
        initInputFields(controlPanel);
        initStartButton(controlPanel);

        panel.add(controlPanel, 3);
    }

    private void initResultsFolderPanel(@NotNull JPanel controlPanel) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.gridx = 0;
        constraints.gridwidth = 4;
        constraints.weighty = 1;
        createPanelWithLabel("Save results to:", resultsFolderTextField, controlPanel, constraints);
    }

    private void initServerHostPanel(@NotNull JPanel controlPanel) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 1;
        constraints.gridx = 0;
        constraints.gridwidth = 4;
        constraints.weighty = 1;
        createPanelWithLabel("Server hostname:", serverHostTextField, controlPanel, constraints);
    }

    private void createPanelWithLabel(
            @NotNull String labelText,
            @NotNull JComponent component,
            @NotNull JPanel controlPanel,
            @NotNull GridBagConstraints constraints
    ) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JLabel label = new JLabel(labelText);
        panel.add(label);
        component.setPreferredSize(new Dimension(500, 24));
        panel.add(component);

        controlPanel.add(panel, constraints);
    }

    private void initStartButton(@NotNull JPanel controlPanel) {
        JButton buttonStart = new JButton("start testing");
        buttonStart.addActionListener(new ButtonStartListener());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 3;
        constraints.gridx = 2;
        controlPanel.add(buttonStart, constraints);
    }

    private void initParameterChangingFields(@NotNull JPanel controlPanel) {
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new GridLayout(4, 2, 0, 0));

        JLabel label = new JLabel("Parameter range:");
        fieldsPanel.add(label);
        label = new JLabel("");
        fieldsPanel.add(label);

        JLabel paramStartValLabel = new JLabel("start value: ");
        fieldsPanel.add(paramStartValLabel);
        fieldsPanel.add(paramStartValTextField);

        JLabel paramEndValLabel = new JLabel("end value: ");
        fieldsPanel.add(paramEndValLabel);
        fieldsPanel.add(paramEndValTextField);

        JLabel paramStepLabel = new JLabel("step: ");
        fieldsPanel.add(paramStepLabel);
        fieldsPanel.add(paramStepTextField);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 2;
        constraints.gridx = 2;
        controlPanel.add(fieldsPanel, constraints);
    }

    private void initInputFields(@NotNull JPanel controlPanel) {
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new GridLayout(6, 2, 0, 0));

        JLabel label = new JLabel("Input data:");
        fieldsPanel.add(label);
        label = new JLabel("");
        fieldsPanel.add(label);

        JLabel xLabel = new JLabel("X (requests count): ");
        fieldsPanel.add(xLabel);
        fieldsPanel.add(xTextField);

        JLabel nLabel = new JLabel("N (array size): ");
        fieldsPanel.add(nLabel);
        fieldsPanel.add(nTextField);

        JLabel mLabel = new JLabel("M (clients count): ");
        fieldsPanel.add(mLabel);
        fieldsPanel.add(mTextField);

        JLabel deltaLabel = new JLabel("delta (time delta, ms): ");
        fieldsPanel.add(deltaLabel);
        fieldsPanel.add(deltaTextField);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 3;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.weighty = 1.0;
        controlPanel.add(fieldsPanel, constraints);
    }

    private void initTestParameter(@NotNull JPanel controlPanel) {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(4, 1, 0, 0));

        JLabel label = new JLabel("Test parameter:");
        buttonsPanel.add(label);

        JRadioButton NParamButton = new JRadioButton(TestParam.N.toString(), true);
        JRadioButton MParamButton = new JRadioButton(TestParam.M.toString(), false);
        JRadioButton deltaParamButton = new JRadioButton(TestParam.delta.toString(), false);
        NParamButton.setActionCommand(TestParam.N.toString());
        MParamButton.setActionCommand(TestParam.M.toString());
        deltaParamButton.setActionCommand(TestParam.delta.toString());
        testParamSelector.add(NParamButton);
        testParamSelector.add(MParamButton);
        testParamSelector.add(deltaParamButton);
        buttonsPanel.add(NParamButton);
        buttonsPanel.add(MParamButton);
        buttonsPanel.add(deltaParamButton);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 2;
        constraints.gridx = 1;
        controlPanel.add(buttonsPanel, constraints);
    }


    private void initArchitecturesTypeChoiceButtons(@NotNull JPanel controlPanel) {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(4, 1, 0, 0));

        JLabel label = new JLabel("Architecture type:");
        buttonsPanel.add(label);


        JRadioButton blockingButton = new JRadioButton(ServerArchitectureType.BLOCKING.toString(), true);
        JRadioButton nonBlockingButton = new JRadioButton(ServerArchitectureType.NON_BLOCKING.toString(), false);
        JRadioButton asynchronousButton = new JRadioButton(ServerArchitectureType.ASYNCHRONOUS.toString(), false);
        blockingButton.setActionCommand(ServerArchitectureType.BLOCKING.toString());
        nonBlockingButton.setActionCommand(ServerArchitectureType.NON_BLOCKING.toString());
        asynchronousButton.setActionCommand(ServerArchitectureType.ASYNCHRONOUS.toString());
        architectureTypeSelector.add(blockingButton);
        architectureTypeSelector.add(nonBlockingButton);
        architectureTypeSelector.add(asynchronousButton);
        buttonsPanel.add(blockingButton);
        buttonsPanel.add(nonBlockingButton);
        buttonsPanel.add(asynchronousButton);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 2;
        constraints.gridx = 0;
        constraints.weighty = 1.0;
        controlPanel.add(buttonsPanel, constraints);
    }

    private void initGraphPanels(@NotNull JPanel panel) {
        panel.add(taskExecutionTimeChart, 0);
        panel.add(clientProcessTimeChart, 1);
        panel.add(requestAverageTimeChart, 2);
    }

    private JFreeChart createChart(@NotNull String title, @NotNull String xAxisLabel, @NotNull XYDataset xyDataset) {
        JFreeChart chart = ChartFactory
                .createScatterPlot(
                        title, xAxisLabel, "time, ms",
                        xyDataset, PlotOrientation.VERTICAL,
                        false, true, true
                );
        chart.getXYPlot().getRenderer();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        chart.getXYPlot().setRenderer(renderer);
        return chart;
    }

    private void showResults(@NotNull ArrayList<TestResult> results, @NotNull TestParam testParam) {
        XYSeries taskExecutionTimeSeries = new XYSeries("");
        XYSeries clientProcessTimeSeries = new XYSeries("");
        XYSeries requestAverageTimeSeries = new XYSeries("");

        for (TestResult result : results) {
            int paramValue = 0;
            switch (testParam) {
                case N:
                    paramValue = result.testCaseInfo.arraySize;
                    break;
                case M:
                    paramValue = result.testCaseInfo.clientsCount;
                    break;
                case delta:
                    paramValue = result.testCaseInfo.timeDeltaBetweenRequests;
            }
            taskExecutionTimeSeries.add(paramValue, result.taskExecutionTime);
            clientProcessTimeSeries.add(paramValue, result.clientProcessTime);
            requestAverageTimeSeries.add(paramValue, result.requestAverageTime);
        }

        taskExecutionTimeChart.setChart(createChart(ChartTitles.taskExecutionTime, testParam.toString(), new XYSeriesCollection(taskExecutionTimeSeries)));
        clientProcessTimeChart.setChart(createChart(ChartTitles.clientProcessTime, testParam.toString(), new XYSeriesCollection(clientProcessTimeSeries)));
        requestAverageTimeChart.setChart(createChart(ChartTitles.requestAverageTime, testParam.toString(), new XYSeriesCollection(requestAverageTimeSeries)));
    }

    private static class ChartTitles {
        public final static String taskExecutionTime = "task execution time";
        public final static String clientProcessTime = "client process time";
        public final static String requestAverageTime = "request average time";

        private ChartTitles() {
        }
    }

    private class ButtonStartListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            ServerArchitectureType architectureType = getArchitectureTypeParam();
            TestParam testParam = getTestParam();

            if (architectureType == null || testParam == null) {
                return;
            }

            int X = getXParam();
            int N = getNParam();
            int M = getMParam();
            int delta = getDeltaParam();

            if (X <= 0 || N <= 0 || M <= 0 || delta < 0) {
                JOptionPane.showMessageDialog(ApplicationGui.this, "wrong input parameters!");
                return;
            }

            int startParamValue = getStartValueParam();
            int endParamValue = getEndValueParam();
            int step = getStepParam();

            if (startParamValue < 0 || (startParamValue == 0 && testParam != TestParam.delta) || endParamValue < 0 || step <= 0 || startParamValue > endParamValue) {
                JOptionPane.showMessageDialog(ApplicationGui.this, "wrong test parameter range!");
                return;
            }

            File resultsFolder = getResultsFolder();
            if (resultsFolder == null) {
                JOptionPane.showMessageDialog(ApplicationGui.this, "wrong download folder!");
                return;
            }

            String serverHost = getServerHost();
            if ("".equals(serverHost)) {
                JOptionPane.showMessageDialog(ApplicationGui.this, "wrong server host!");
                return;
            }

            ArrayList<TestResult> results = TestsRunner.test(
                    resultsFolder,
                    serverHost,
                    architectureType,
                    N,
                    M,
                    delta,
                    X,
                    testParam,
                    startParamValue,
                    endParamValue,
                    step
            );

            showResults(results, testParam);
        }
    }
}