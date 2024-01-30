package gui;

import ga.GAEvent;
import ga.GAListener;
import ga.GeneticAlgorithm;
import ga.geneticOperators.*;
import ga.selectionMethods.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import project.Project;
import project.ProjectIndividual;

public class MainFrame extends JFrame implements GAListener {

    private static final long serialVersionUID = 1L;
    private Project project;
    private GeneticAlgorithm<ProjectIndividual, Project> ga;
    private JEditorPane problemPanel;
    private JEditorPane bestIndividualPanel;
    private JScrollPane bestIndividualScrollPanel;
    private JScrollPane problemScrollPanel;
    private PanelParameters panelParameters = new PanelParameters();
    private JButton buttonDataSet = new JButton("Data set");
    private JButton buttonRun = new JButton("Start");
    private JButton buttonStop = new JButton("Stop");
    public JButton buttonCutting = new JButton("Start plotter");
    private XYSeries seriesBestIndividual;
    private XYSeries seriesAverage;
    private SwingWorker<Void, Void> worker;
    private cuttingThread cuttingThread = new cuttingThread();
    private ProjectIndividual bestResult;

    public MainFrame() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private void jbInit() throws Exception {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("2d-cutting-stock-problem");
        ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("icon.png"));
        this.setIconImage(icon.getImage());

        //North Left Panel
        JPanel panelNorthLeft = new JPanel(new BorderLayout());
        panelNorthLeft.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(""),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)));

        panelNorthLeft.add(panelParameters, java.awt.BorderLayout.WEST);
        JPanel panelButtons = new JPanel();
        panelButtons.add(buttonDataSet);
        buttonDataSet.addActionListener(new ButtonDataSet_actionAdapter(this));
        panelButtons.add(buttonRun);
        buttonRun.setEnabled(false);
        buttonRun.addActionListener(new ButtonRun_actionAdapter(this));
        panelButtons.add(buttonStop);
        buttonStop.setEnabled(false);
        buttonStop.addActionListener(new ButtonStop_actionAdapter(this));
        panelButtons.add(buttonCutting);
        buttonCutting.setEnabled(false);
        buttonCutting.addActionListener(new ButtonCutting_actionAdapter(this));
        panelNorthLeft.add(panelButtons, java.awt.BorderLayout.SOUTH);

        //North Right Panel - Chart creation
        seriesBestIndividual = new XYSeries("Best individual");
        seriesAverage = new XYSeries("Avarage of population");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(seriesBestIndividual);
        dataset.addSeries(seriesAverage);
        JFreeChart chart = ChartFactory.createXYLineChart("Evolution", // Title
                "Generation", // x-axis Label
                "Success rate", // y-axis Label
                dataset, // Dataset
                PlotOrientation.VERTICAL, // Plot Orientation
                true, // Show Legend
                true, // Use tooltips
                false // Configure chart to generate URLs?
        );
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(""),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        // default size
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

        //North Panel
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(panelNorthLeft, java.awt.BorderLayout.WEST);
        northPanel.add(chartPanel, java.awt.BorderLayout.CENTER);

        //Center panel
        //JPanel panel = new JPanel();
        //panel.setPreferredSize(new Dimension(20, 20));
        problemPanel = new JEditorPane();
        problemPanel.setContentType("text/html");
        problemPanel.setPreferredSize(new Dimension(300, 300));
        problemScrollPanel = new JScrollPane(problemPanel);

        bestIndividualPanel = new JEditorPane();
        bestIndividualPanel.setContentType("text/html");
        bestIndividualPanel.setPreferredSize(new Dimension(300, 300));
        bestIndividualScrollPanel = new JScrollPane(bestIndividualPanel);

        JPanel centerPanel = new JPanel(new BorderLayout());
        //centerPanel.add(panel, java.awt.BorderLayout.WEST);
        centerPanel.add(problemScrollPanel, java.awt.BorderLayout.WEST);
        centerPanel.add(bestIndividualScrollPanel, java.awt.BorderLayout.CENTER);

        //Global structure
        JPanel globalPanel = new JPanel(new BorderLayout());
        globalPanel.add(northPanel, java.awt.BorderLayout.NORTH);
        globalPanel.add(centerPanel, java.awt.BorderLayout.CENTER);
        //globalPanel.add(southPanel, java.awt.BorderLayout.SOUTH);
        this.getContentPane().add(globalPanel);

        pack();
    }

    public void buttonDataSet_actionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser(new java.io.File("."));
        int returnVal = fc.showOpenDialog(this);
        int test;

        try {
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File dataSet = fc.getSelectedFile();
                project = Project.buildProject(dataSet);
                test = project.getNumPieces();

                String problemString = "";

                for (int i = 0; i < project.getNumPieces(); i++) {
                    problemString += "P" + project.getPiece(i).number + "<br>";
                    for (int l = 0; l < project.getPiece(i).lines; l++) {
                        for (int c = 0; c < project.getPiece(i).columns; c++) {
                            if (project.getPiece(i).matrix[l][c] == 0) {
                                problemString += "<font color=#FFFFFF>\u25a1</font>";
                            } else {
                                problemString += "<font color=" + (project.getColor(i)) + ">\u25a0</font>";
                            }
                        }
                        problemString += "<br>";
                    }
                    problemString += "<br>";
                }

                problemPanel.setText(problemString);
                buttonRun.setEnabled(true);
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.err);
        } catch (java.util.NoSuchElementException e2) {
            JOptionPane.showMessageDialog(this, "File format not valid", "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void jButtonRun_actionPerformed(ActionEvent e) {
        try {
            if (project == null) {
                JOptionPane.showMessageDialog(this, "You must first choose a problem", "Error!", JOptionPane.ERROR_MESSAGE);
                return;
            }

            //bestIndividualPanel.textArea.setText("");
            bestResult = null;
            bestIndividualPanel.setText("");
            seriesBestIndividual.clear();
            seriesAverage.clear();

            project.setFitnessType(panelParameters.jComboBoxFitnessTypes.getSelectedIndex());

            ga = new GeneticAlgorithm<ProjectIndividual, Project>(
                    Integer.parseInt(panelParameters.jTextFieldN.getText()),
                    Integer.parseInt(panelParameters.jTextFieldGenerations.getText()),
                    panelParameters.getSelectionMethod(),
                    panelParameters.getRecombinationMethod(),
                    panelParameters.getMutationMethod(),
                    new Random(Integer.parseInt(panelParameters.jTextFieldSeed.getText())));

            System.out.println("Fitness type: " + project.getFitnessType());
            System.out.println(ga);

            ga.addGAListener(this);

            manageButtons(false, false, true, false);

            worker = new SwingWorker<Void, Void>() {
                public Void doInBackground() {
                    try {

                        ga.run(project);

                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                    return null;
                }

                @Override
                public void done() {
                    manageButtons(true, true, false, true);
                }
            };

            worker.execute();

        } catch (NumberFormatException e1) {
            JOptionPane.showMessageDialog(this, "Wrong parameters!", "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void generationEnded(GAEvent e) {
        GeneticAlgorithm<ProjectIndividual, Project> source = e.getSource();
        if(bestResult != null)
        {
        	if(bestResult.getFitness() < source.getBestInRun().getFitness())
        	{
            	bestResult = null;
                bestResult = source.getBestInRun();
                bestIndividualPanel.setText(source.getBestInRun().toStringWithColors());
        	}
        }
        else
        {
            bestResult = source.getBestInRun();
        	bestIndividualPanel.setText(source.getBestInRun().toStringWithColors());
        }
        seriesBestIndividual.add(source.getGeneration(), source.getBestInRun().getFitness());
        seriesAverage.add(source.getGeneration(), source.getAverageFitness());
        if (worker.isCancelled()) {
            e.setStopped(true);
        }
    }
    
    public void GraphicPanelUpdate(int part)
    {
    	bestIndividualPanel.setText(bestResult.toStringWithColors(part));
    }

    public void runEnded(GAEvent e) {
        GeneticAlgorithm<ProjectIndividual, Project> source = e.getSource();
        System.out.println(source.getBestInRun().toStringAsMatrix());

        new Thread(new Runnable() {
            @Override
            public void run() {
            	try {
                    Files.write(Path.of("gCodeGenerator/bestMatrix.txt"), source.getBestInRun().toStringAsMatrix().getBytes());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                
                ProcessBuilder processBuilder = new ProcessBuilder("python", "final.py");
                processBuilder.directory(new File("gCodeGenerator"));
                try {
                    Process process = processBuilder.start();
                    // blocked :(
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }

                    int exitCode = process.waitFor();
                    System.out.println("\nExited with error code : " + exitCode);

                } catch (IOException IO_Exception) {
                    IO_Exception.printStackTrace();
                } catch (InterruptedException Interrupt_Exception) {
                    Interrupt_Exception.printStackTrace();
                }
            }
       }).start();
    }

    public void jButtonStop_actionPerformed(ActionEvent e) {
        worker.cancel(true);
    }

    public void jButtonCutting_actionPerformed(ActionEvent e) {
    	if(!cuttingThread.isAlive())
    	{
    		cuttingThread = null;
    		cuttingThread = new cuttingThread();
    		cuttingThread.parent = this;
        	cuttingThread.start();
        	buttonCutting.setText("End plotting");
        	manageButtons(false, false, false, true);
    	}
    	else
    	{
    		cuttingThread.processKill();
        	buttonCutting.setText("Start plotting");
        	manageButtons(true, true, false, true);
    	}
    }
    
    public void manageButtons(
            boolean dataSet,
            boolean run,
            boolean stopRun,
            boolean cutting) {

        buttonDataSet.setEnabled(dataSet);
        buttonRun.setEnabled(run);
        buttonStop.setEnabled(stopRun);
        buttonCutting.setEnabled(cutting);
    }

}

class cuttingThread extends Thread {
	public Process process;
	public MainFrame parent;
	
	@Override 
	public void run() 
	{
		ProcessBuilder processBuilder = new ProcessBuilder("python", "sender.py", "bestMatrix.gcode", "COM51");
        processBuilder.directory(new File("gCodeSender"));
        try {
        	process = processBuilder.start();
            BufferedReader is = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = is.readLine()) != null)
            {
            	int index = 0;
                String[] arr = new String[line.length()];
            	//parent.GraphicPanelUpdate(Integer.parseInt(line));
            	for(int i = 0; i < line.length(); i++)
            	{
            		arr = line.split("");
            		try
            		{
            			if(Integer.parseInt(arr[i]) >= 0 & Integer.parseInt(arr[i]) <= 9)
            			{
            				index = i;
            			}
            			else
            			{
            				break;
            			}
            		}
            		catch(NumberFormatException e)
            		{
            			
            		}
            	}	
            	if(line.length() >= index+1)
            	{
                    String firstHalf = line.substring(0,index+1);
            		System.out.println(Integer.parseInt(firstHalf));
            		parent.GraphicPanelUpdate(Integer.parseInt(firstHalf));
            	}
            }
            
            int exitCode = process.waitFor();
            System.out.println("\nExited with error code : " + exitCode);
            parent.buttonCutting.setText("Start Plotting");
            parent.manageButtons(true, true, false, true);

        } catch (IOException IO_Exception) {
            IO_Exception.printStackTrace();
        } catch (InterruptedException Interrupt_Exception) {
            Interrupt_Exception.printStackTrace();
        }
        System.out.println("Cutting Process is Finished.");
	}
	
	public void processKill()
	{
		process.destroy();
	}
}

class ButtonDataSet_actionAdapter implements ActionListener {

    private MainFrame adaptee;

    ButtonDataSet_actionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.buttonDataSet_actionPerformed(e);
    }
}

class ButtonRun_actionAdapter implements ActionListener {

    private MainFrame adaptee;

    ButtonRun_actionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButtonRun_actionPerformed(e);
    }
}

class ButtonStop_actionAdapter implements ActionListener {

    private MainFrame adaptee;

    ButtonStop_actionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButtonStop_actionPerformed(e);
    }
}

class ButtonCutting_actionAdapter implements ActionListener {

    private MainFrame adaptee;

    ButtonCutting_actionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButtonCutting_actionPerformed(e);
    }
}

class PanelTextArea extends JPanel {

    JTextArea textArea;

    public PanelTextArea(String title, int rows, int columns) {
        textArea = new JTextArea(rows, columns);
        setLayout(new BorderLayout());
        add(new JLabel(title), java.awt.BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setEditable(false);
        add(scrollPane);
    }
}

class PanelAtributesValue extends JPanel {

    protected String title;
    protected List<JLabel> labels = new ArrayList<JLabel>();
    protected List<JComponent> valueComponents = new ArrayList<JComponent>();

    public PanelAtributesValue() {
    }

    protected void configure() {

        //for(JComponent textField : textFields)
        //textField.setHorizontalAlignment(SwingConstants.RIGHT);
        GridBagLayout gridbag = new GridBagLayout();
        setLayout(gridbag);

        //addLabelTextRows
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHEAST;
        Iterator<JLabel> itLabels = labels.iterator();
        Iterator<JComponent> itTextFields = valueComponents.iterator();

        while (itLabels.hasNext()) {
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;                       //reset to default
            add(itLabels.next(), c);

            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            add(itTextFields.next(), c);
        }
    }
}

class PanelParameters extends PanelAtributesValue {

    public static final int TEXT_FIELD_LENGHT = 7;
    public static final String SEED = "42";
    public static final String POPULATION_SIZE = "200";
    public static final String GENERATIONS = "1000";
    public static final String TOURNAMENT_SIZE = "2";
    public static final String PROB_RECOMBINATION = "0.7";
    public static final String PROB_MUTATION = "0.15";
    public static final String PROB_1S = "0.05";
    JTextField jTextFieldSeed = new JTextField(SEED, TEXT_FIELD_LENGHT);
    JTextField jTextFieldN = new JTextField(POPULATION_SIZE, TEXT_FIELD_LENGHT);
    JTextField jTextFieldGenerations = new JTextField(GENERATIONS, TEXT_FIELD_LENGHT);
    String[] selectionMethods = {"Tournament", "Roulette wheel"};
    JComboBox jComboBoxSelectionMethods = new JComboBox(selectionMethods);
    JTextField jTextFieldTournamentSize = new JTextField(TOURNAMENT_SIZE, TEXT_FIELD_LENGHT);
    String[] recombinationMethods = {"Ordered Crossover", "Cycle Crossover", "Partially matched crossover"};
    JComboBox jComboBoxRecombinationMethods = new JComboBox(recombinationMethods);
    JTextField jTextFieldProbRecombination = new JTextField(PROB_RECOMBINATION, TEXT_FIELD_LENGHT);
    JTextField jTextFieldProbMutation = new JTextField(PROB_MUTATION, TEXT_FIELD_LENGHT);
    JTextField jTextFieldProb1s = new JTextField(PROB_1S, TEXT_FIELD_LENGHT);
    String[] fitnessTypes = {"simple", "with penalty"};
    JComboBox jComboBoxFitnessTypes = new JComboBox(fitnessTypes);

    public PanelParameters() {
        title = "Genetic algorithm parameters";

        /*labels.add(new JLabel("Seed: "));
        valueComponents.add(jTextFieldSeed);
        jTextFieldSeed.addKeyListener(new IntegerTextField_KeyAdapter(null));*/

        labels.add(new JLabel("Population Number: "));
        valueComponents.add(jTextFieldN);
        jTextFieldN.addKeyListener(new IntegerTextField_KeyAdapter(null));

        labels.add(new JLabel("Number of Generations: "));
        valueComponents.add(jTextFieldGenerations);
        jTextFieldGenerations.addKeyListener(new IntegerTextField_KeyAdapter(null));

        labels.add(new JLabel("Selection Method: "));
        valueComponents.add(jComboBoxSelectionMethods);
        jComboBoxSelectionMethods.addActionListener(new JComboBoxSelectionMethods_ActionAdapter(this));

        /*labels.add(new JLabel("Tournament size: "));
        valueComponents.add(jTextFieldTournamentSize);
        jTextFieldTournamentSize.addKeyListener(new IntegerTextField_KeyAdapter(null));*/

        labels.add(new JLabel("Recombination Method: "));
        valueComponents.add(jComboBoxRecombinationMethods);

        labels.add(new JLabel("Possibility of Recombination: "));
        valueComponents.add(jTextFieldProbRecombination);

        labels.add(new JLabel("Mutation Possibility: "));
        valueComponents.add(jTextFieldProbMutation);

        labels.add(new JLabel("First place's starting rate: "));
        valueComponents.add(jTextFieldProb1s);

        configure();
    }

    public void actionPerformedSelectionMethods(ActionEvent e) {
        if (jComboBoxFitnessTypes.getSelectedIndex() == 1
                && jComboBoxSelectionMethods.getSelectedIndex() == 1) {
            jComboBoxSelectionMethods.setSelectedIndex(0);
            JOptionPane.showMessageDialog(this, "Not allowed with penalty fitness", "Error!", JOptionPane.ERROR_MESSAGE);
        }
        jTextFieldTournamentSize.setEnabled((jComboBoxSelectionMethods.getSelectedIndex() == 0) ? true : false);
    }

    public SelectionMethod<ProjectIndividual, Project> getSelectionMethod() {
        switch (jComboBoxSelectionMethods.getSelectedIndex()) {
            case 0:
                return new Tournament<ProjectIndividual, Project>(
                        Integer.parseInt(jTextFieldN.getText()),
                        Integer.parseInt(jTextFieldTournamentSize.getText()));
            case 1:
                return new RouletteWheel<ProjectIndividual, Project>(
                        Integer.parseInt(jTextFieldN.getText()));
        }
        return null;
    }

    public Recombination<ProjectIndividual> getRecombinationMethod() {

        double recombinationProb = Double.parseDouble(jTextFieldProbRecombination.getText());

        switch (jComboBoxRecombinationMethods.getSelectedIndex()) {
            case 0:
                return new OrderedCrossover<ProjectIndividual>(recombinationProb);
            case 1:
                return new CycleCrossover<ProjectIndividual>(recombinationProb);
            case 2:
                return new RecombinationUniform<ProjectIndividual>(recombinationProb);
        }
        return null;
    }

    public PieceMutation<ProjectIndividual> getMutationMethod() {
        double mutationProb = Double.parseDouble(jTextFieldProbMutation.getText());
        return new PieceMutation<ProjectIndividual>(mutationProb);
    }

    public void actionPerformedFitnessType(ActionEvent e) {
        if (jComboBoxFitnessTypes.getSelectedIndex() == 1
                && jComboBoxSelectionMethods.getSelectedIndex() == 1) {
            jComboBoxFitnessTypes.setSelectedIndex(0);
            JOptionPane.showMessageDialog(this, "Not allowed with roulette wheel", "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class JComboBoxSelectionMethods_ActionAdapter implements ActionListener {

    private PanelParameters adaptee;

    JComboBoxSelectionMethods_ActionAdapter(PanelParameters adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.actionPerformedSelectionMethods(e);
    }
}

class JComboBoxFitnessFunction_ActionAdapter implements ActionListener {

    private PanelParameters adaptee;

    JComboBoxFitnessFunction_ActionAdapter(PanelParameters adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.actionPerformedFitnessType(e);
    }
}

class IntegerTextField_KeyAdapter implements KeyListener {

    private MainFrame adaptee;

    IntegerTextField_KeyAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (!Character.isDigit(c) || c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE) {
            e.consume();
        }
    }
}
