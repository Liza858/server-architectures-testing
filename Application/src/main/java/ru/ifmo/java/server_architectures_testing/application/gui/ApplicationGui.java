package ru.ifmo.java.server_architectures_testing.application.gui;

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
import ru.ifmo.java.server_architectures_testing.application.logic.TestParametersApplication;
import ru.ifmo.java.server_architectures_testing.application.logic.TestResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ApplicationGui extends JFrame {

    private final JTextField xTextField = new JTextField("10");
    private final JTextField nTextField = new JTextField("500");
    private final JTextField mTextField = new JTextField("5");
    private final JTextField deltaTextField = new JTextField("0");
    private final JTextField paramStartValTextField = new JTextField("10");
    private final JTextField paramEndValTextField = new JTextField("1000");
    private final JTextField paramStepTextField = new JTextField("200");
    private final ButtonGroup architectureTypeSelector = new ButtonGroup();
    private final ButtonGroup testParameterSelector = new ButtonGroup();
    private final TestParametersApplication testParametersApplication = new TestParametersApplication(10);
    private final ChartPanel requestAverageTimeChart;
    private final ChartPanel clientProcessTimeChart;
    private final ChartPanel taskExecutionTimeChart;

    public ApplicationGui() {
        super("Server architectures testing application");
        this.setBounds(300, 50, 600, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        XYSeries series = new XYSeries("");
        XYDataset xyDataset = new XYSeriesCollection(series);
        requestAverageTimeChart = new ChartPanel(createChart(ChartTitles.requestAverageTime, "N", xyDataset));
        clientProcessTimeChart = new ChartPanel(createChart(ChartTitles.clientProcessTime, "N", xyDataset));
        taskExecutionTimeChart = new ChartPanel(createChart(ChartTitles.taskExecutionTime, "N", xyDataset));

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
        switch (buttonModel.getActionCommand()) {
            case ArchitectureTypeButtonActionCommand.blocking:
                return ServerArchitectureType.BLOCKING;
            case ArchitectureTypeButtonActionCommand.nonBlocking:
                return ServerArchitectureType.NON_BLOCKING;
            case ArchitectureTypeButtonActionCommand.asynchronous:
                return ServerArchitectureType.ASYNCHRONOUS;
        }
        return null;
    }

    private TestParam getTestParam() {
        ButtonModel buttonModel = testParameterSelector.getSelection();
        switch (buttonModel.getActionCommand()) {
            case TestParamButtonActionCommand.N:
                return TestParam.N;
            case TestParamButtonActionCommand.M:
                return TestParam.M;
            case TestParamButtonActionCommand.delta:
                return TestParam.delta;
        }
        return null;
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


    private int getIntParamFromString(String maybeNumber) {
        try {
            return Integer.parseInt(maybeNumber);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void initControlPanel(JPanel panel) {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());

        initArchitecturesTypeChoiceButtons(controlPanel);
        initTestParameter(controlPanel);
        initParameterChangingFields(controlPanel);
        initInputFields(controlPanel);
        initStartButton(controlPanel);

        panel.add(controlPanel, 3);
    }

    private void initStartButton(JPanel controlPanel) {
        JButton buttonStart = new JButton("start testing");
        buttonStart.addActionListener(new ButtonStartListener());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 1;
        constraints.gridx = 2;
        controlPanel.add(buttonStart, constraints);
    }

    private void initParameterChangingFields(JPanel controlPanel) {
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
        constraints.gridy = 0;
        constraints.gridx = 2;
        controlPanel.add(fieldsPanel, constraints);
    }

    private void initInputFields(JPanel controlPanel) {
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new GridLayout(5, 2, 0, 0));

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
        constraints.gridy = 1;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.weighty = 1.0;
        controlPanel.add(fieldsPanel, constraints);
    }

    private void initTestParameter(JPanel controlPanel) {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(4, 1, 0, 0));

        JLabel label = new JLabel("Test parameter:");
        buttonsPanel.add(label);

        JRadioButton blockingButton = new JRadioButton("N", true);
        JRadioButton nonBlockingButton = new JRadioButton("M", false);
        JRadioButton asynchronousButton = new JRadioButton("delta", false);
        blockingButton.setActionCommand(TestParamButtonActionCommand.N);
        nonBlockingButton.setActionCommand(TestParamButtonActionCommand.M);
        asynchronousButton.setActionCommand(TestParamButtonActionCommand.delta);
        testParameterSelector.add(blockingButton);
        testParameterSelector.add(nonBlockingButton);
        testParameterSelector.add(asynchronousButton);
        buttonsPanel.add(blockingButton);
        buttonsPanel.add(nonBlockingButton);
        buttonsPanel.add(asynchronousButton);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.gridx = 1;
        controlPanel.add(buttonsPanel, constraints);
    }


    private void initArchitecturesTypeChoiceButtons(JPanel controlPanel) {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(4, 1, 0, 0));

        JLabel label = new JLabel("Architecture type:");
        buttonsPanel.add(label);


        JRadioButton blockingButton = new JRadioButton("blocking", true);
        JRadioButton nonBlockingButton = new JRadioButton("non blocking", false);
        JRadioButton asynchronousButton = new JRadioButton("asynchronous", false);
        blockingButton.setActionCommand(ArchitectureTypeButtonActionCommand.blocking);
        nonBlockingButton.setActionCommand(ArchitectureTypeButtonActionCommand.nonBlocking);
        asynchronousButton.setActionCommand(ArchitectureTypeButtonActionCommand.asynchronous);
        architectureTypeSelector.add(blockingButton);
        architectureTypeSelector.add(nonBlockingButton);
        architectureTypeSelector.add(asynchronousButton);
        buttonsPanel.add(blockingButton);
        buttonsPanel.add(nonBlockingButton);
        buttonsPanel.add(asynchronousButton);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.gridx = 0;
        constraints.weighty = 1.0;
        controlPanel.add(buttonsPanel, constraints);
    }

    private void initGraphPanels(JPanel panel) {
        panel.add(taskExecutionTimeChart, 0);
        panel.add(clientProcessTimeChart, 1);
        panel.add(requestAverageTimeChart, 2);
    }

    private JFreeChart createChart(String title, String xAxisLabel, XYDataset xyDataset) {
        JFreeChart chart = ChartFactory
                .createScatterPlot(title, xAxisLabel, "time, ms",
                        xyDataset,
                        PlotOrientation.VERTICAL,
                        false, true, true);
        chart.getXYPlot().getRenderer();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        chart.getXYPlot().setRenderer(renderer);
        return chart;
    }

    private void showResults(ArrayList<TestResult> results, TestParam testParam) {
        XYSeries taskExecutionTimeSeries = new XYSeries("");
        XYSeries clientProcessTimeSeries = new XYSeries("");
        XYSeries requestAverageTimeSeries = new XYSeries("");

        for (TestResult result : results) {
            switch (testParam) {
                case N:
                    taskExecutionTimeSeries.add(result.testParameters.arraySize, result.taskExecutionTime);
                    clientProcessTimeSeries.add(result.testParameters.arraySize, result.clientProcessTime);
                    requestAverageTimeSeries.add(result.testParameters.arraySize, result.requestAverageTime);
                    break;
                case M:
                    taskExecutionTimeSeries.add(result.testParameters.clientsCount, result.taskExecutionTime);
                    clientProcessTimeSeries.add(result.testParameters.clientsCount, result.clientProcessTime);
                    requestAverageTimeSeries.add(result.testParameters.clientsCount, result.requestAverageTime);
                    break;
                case delta:
                    taskExecutionTimeSeries.add(result.testParameters.timeDeltaBetweenRequests, result.taskExecutionTime);
                    clientProcessTimeSeries.add(result.testParameters.timeDeltaBetweenRequests, result.clientProcessTime);
                    requestAverageTimeSeries.add(result.testParameters.timeDeltaBetweenRequests, result.requestAverageTime);
                    break;
            }
        }

        taskExecutionTimeChart.setChart(createChart(ChartTitles.taskExecutionTime, testParam.toString(), new XYSeriesCollection(taskExecutionTimeSeries)));
        clientProcessTimeChart.setChart(createChart(ChartTitles.clientProcessTime, testParam.toString(), new XYSeriesCollection(clientProcessTimeSeries)));
        requestAverageTimeChart.setChart(createChart(ChartTitles.requestAverageTime, testParam.toString(), new XYSeriesCollection(requestAverageTimeSeries)));
    }

    private static class TestParamButtonActionCommand {
        public final static String N = "N";
        public final static String M = "M";
        public final static String delta = "delta";

        private TestParamButtonActionCommand() {
        }
    }

    private static class ArchitectureTypeButtonActionCommand {
        public final static String blocking = "blocking";
        public final static String nonBlocking = "nonBlocking";
        public final static String asynchronous = "delta";

        private ArchitectureTypeButtonActionCommand() {
        }
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
                JOptionPane.showMessageDialog(ApplicationGui.this, "error input parameters!");
                return;
            }

            int startParamValue = getStartValueParam();
            int endParamValue = getEndValueParam();
            int step = getStepParam();

            if (startParamValue < 0 || (startParamValue == 0 && testParam != TestParam.delta) || endParamValue < 0 || step <= 0 || startParamValue > endParamValue) {
                JOptionPane.showMessageDialog(ApplicationGui.this, "error test parameter range!");
                return;
            }

            ArrayList<TestResult> results = null;
            switch (testParam) {
                case N:
                    results = testParametersApplication.testDifferentArraySize(
                            architectureType,
                            M,
                            X,
                            delta,
                            startParamValue,
                            endParamValue,
                            step
                    );
                    break;
                case M:
                    results = testParametersApplication.testDifferentClientsCount(
                            architectureType,
                            N,
                            X,
                            delta,
                            startParamValue,
                            endParamValue,
                            step
                    );
                    break;
                case delta:
                    results = testParametersApplication.testDifferentTimeDelta(
                            architectureType,
                            M,
                            X,
                            N,
                            startParamValue,
                            endParamValue,
                            step
                    );
                    break;
            }

            showResults(results, testParam);
        }
    }
}