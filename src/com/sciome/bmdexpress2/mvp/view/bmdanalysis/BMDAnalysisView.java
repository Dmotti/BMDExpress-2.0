package com.sciome.bmdexpress2.mvp.view.bmdanalysis;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.sciome.bmdexpress2.mvp.model.IStatModelProcessable;
import com.sciome.bmdexpress2.mvp.presenter.bmdanalysis.BMDAnalysisPresenter;
import com.sciome.bmdexpress2.mvp.view.BMDExpressViewBase;
import com.sciome.bmdexpress2.mvp.viewinterface.bmdanalysis.IBMDAnalysisView;
import com.sciome.bmdexpress2.shared.BMDExpressProperties;
import com.sciome.bmdexpress2.shared.eventbus.BMDExpressEventBus;
import com.sciome.bmdexpress2.util.bmds.ModelInputParameters;
import com.sciome.bmdexpress2.util.bmds.ModelSelectionParameters;
import com.sciome.bmdexpress2.util.bmds.shared.BestModelSelectionWithFlaggedHillModelEnum;
import com.sciome.bmdexpress2.util.bmds.shared.BestPolyModelTestEnum;
import com.sciome.bmdexpress2.util.bmds.shared.ExponentialModel;
import com.sciome.bmdexpress2.util.bmds.shared.FlagHillModelDoseEnum;
import com.sciome.bmdexpress2.util.bmds.shared.HillModel;
import com.sciome.bmdexpress2.util.bmds.shared.PolyModel;
import com.sciome.bmdexpress2.util.bmds.shared.PowerModel;
import com.sciome.bmdexpress2.util.bmds.shared.StatModel;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BMDAnalysisView extends BMDExpressViewBase implements IBMDAnalysisView, Initializable
{

	BMDAnalysisPresenter presenter;

	// FXML injection

	// checkboxes
	@FXML
	private CheckBox exponential2CheckBox;
	@FXML
	private CheckBox exponential3CheckBox;
	@FXML
	private CheckBox exponential4CheckBox;
	@FXML
	private CheckBox exponential5CheckBox;
	@FXML
	private CheckBox hillCheckBox;
	@FXML
	private CheckBox powerCheckBox;
	@FXML
	private CheckBox linearCheckBox;
	@FXML
	private CheckBox poly2CheckBox;
	@FXML
	private CheckBox poly3CheckBox;
	@FXML
	private CheckBox poly4CheckBox;

	@FXML
	private CheckBox constantVarianceCheckBox;

	@FXML
	private CheckBox flagHillkParamCheckBox;
	@FXML
	private CheckBox setThreadCheckBox;

	// textfields
	@FXML
	private TextField maximumIterationsTextField;
	@FXML
	private ComboBox bMRFactorComboBox;
	@FXML
	private TextField modifyFlaggedHillBMDTextField;

	// ComboBoxes
	@FXML
	private ComboBox confidenceLevelComboBox;
	@FXML
	private ComboBox restrictPowerComboBox;

	@FXML
	private ComboBox bestPolyTestComboBox;
	@FXML
	private ComboBox pValueCutoffComboBox;

	@FXML
	private ComboBox flagHillkParamComboBox;
	@FXML
	private ComboBox bestModelSeletionWithFlaggedHillComboBox;

	@FXML
	private ComboBox numberOfThreadsComboBox;

	// labels
	@FXML
	private Label expressionDataLabel;
	@FXML
	private Label oneWayANOVADataLabel;
	@FXML
	private Label oneWayANOVADataLabelLabel;
	@FXML
	private Label modifyFlaggedHillBMDLabel;
	@FXML
	private Label bestModelSeletionWithFlaggedHillLabel;
	@FXML
	private Label restrictPowerLabel;

	@FXML
	private ProgressBar progressBar;
	@FXML
	private Label progressLabel;

	@FXML
	private Button startButton;
	@FXML
	private Button cancelButton;

	@FXML
	private VBox mainVBox;
	// anchor panes
	@FXML
	private AnchorPane startCancelPane;
	@FXML
	private AnchorPane threadPane;
	@FXML
	private AnchorPane modelSelectionPane;
	@FXML
	private AnchorPane parametersPane;
	@FXML
	private AnchorPane modelsPane;
	@FXML
	private AnchorPane dataOptionsPane;

	private List<IStatModelProcessable> processableData;

	private boolean selectModelsOnly = false;

	public BMDAnalysisView()
	{
		this(BMDExpressEventBus.getInstance());
	}

	/*
	 * Event bus is passed as an argument so the unit tests can pass their own custom eventbus
	 */
	public BMDAnalysisView(BMDExpressEventBus eventBus)
	{
		super();
		presenter = new BMDAnalysisPresenter(this, eventBus);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.progressBar.setVisible(false);
		this.exponential2CheckBox.setDisable(false);

	}

	/*
	 * use clicked close button
	 */
	@Override
	public void handle_close(ActionEvent event)
	{

	}

	/*
	 * use clicked start button
	 */
	@Override
	public void handle_start(ActionEvent event)
	{
		// create InputParameters object based on things that are selected.

		this.progressBar.setVisible(true);
		ModelInputParameters inputParameters = assignParameters();
		ModelSelectionParameters modelSectionParameters = assignModelSelectionParameters();
		List<StatModel> modelsToRun = new ArrayList<>();
		if (hillCheckBox.isSelected())
		{
			HillModel hillModel = new HillModel();
			hillModel.setVersion(BMDExpressProperties.getInstance().getHillVersion());
			modelsToRun.add(hillModel);
		}
		if (powerCheckBox.isSelected())
		{
			PowerModel powerModel = new PowerModel();
			powerModel.setVersion(BMDExpressProperties.getInstance().getPowerVersion());
			modelsToRun.add(powerModel);
		}
		if (linearCheckBox.isSelected())
		{
			PolyModel linearModel = new PolyModel();
			linearModel.setVersion(BMDExpressProperties.getInstance().getPolyVersion());
			linearModel.setDegree(1);
			modelsToRun.add(linearModel);
		}
		if (poly2CheckBox.isSelected())

		{
			PolyModel poly2Model = new PolyModel();
			poly2Model.setDegree(2);
			poly2Model.setVersion(BMDExpressProperties.getInstance().getPolyVersion());
			modelsToRun.add(poly2Model);
		}
		if (poly3CheckBox.isSelected())
		{
			PolyModel poly3Model = new PolyModel();
			poly3Model.setDegree(3);
			poly3Model.setVersion(BMDExpressProperties.getInstance().getPolyVersion());
			modelsToRun.add(poly3Model);
		}
		if (poly4CheckBox.isSelected())
		{
			PolyModel poly4Model = new PolyModel();
			poly4Model.setDegree(4);
			poly4Model.setVersion(BMDExpressProperties.getInstance().getPolyVersion());
			modelsToRun.add(poly4Model);
		}

		if (exponential2CheckBox.isSelected())
		{
			ExponentialModel exponentialModel = new ExponentialModel();
			exponentialModel.setVersion(BMDExpressProperties.getInstance().getExponentialVersion());
			exponentialModel.setOption(2);
			modelsToRun.add(exponentialModel);
		}
		if (exponential3CheckBox.isSelected())
		{
			ExponentialModel exponentialModel = new ExponentialModel();
			exponentialModel.setVersion(BMDExpressProperties.getInstance().getExponentialVersion());
			modelsToRun.add(exponentialModel);
			exponentialModel.setOption(3);
		}
		if (exponential4CheckBox.isSelected())
		{
			ExponentialModel exponentialModel = new ExponentialModel();
			exponentialModel.setVersion(BMDExpressProperties.getInstance().getExponentialVersion());
			exponentialModel.setOption(4);
			modelsToRun.add(exponentialModel);
		}
		if (exponential5CheckBox.isSelected())
		{
			ExponentialModel exponentialModel = new ExponentialModel();
			exponentialModel.setVersion(BMDExpressProperties.getInstance().getExponentialVersion());
			exponentialModel.setOption(5);
			modelsToRun.add(exponentialModel);
		}
		// start the BMD Analysis
		if (selectModelsOnly)
			presenter.performReselectParameters(inputParameters, modelSectionParameters);
		else
			presenter.performBMDAnalysis(inputParameters, modelSectionParameters, modelsToRun);
	}

	/*
	 * use clicked done button
	 */
	@Override
	public void handle_cancel(ActionEvent event)
	{
		// if presenter cancels the process it will return true
		// otherwise interpret to cancel and close the window
		if (!this.progressBar.isVisible() || this.selectModelsOnly)
		{
			this.closeWindow();
		}
		else
		{
			presenter.cancel();
		}

	}

	private void setModifyBMDOfFlaggedHillEnabledness()
	{
		if (bestModelSeletionWithFlaggedHillComboBox.getSelectionModel().getSelectedIndex() == 3
				&& !flagHillkParamCheckBox.isDisable())
		{
			modifyFlaggedHillBMDLabel.setDisable(false);
			modifyFlaggedHillBMDTextField.setDisable(false);
		}
		else
		{
			modifyFlaggedHillBMDLabel.setDisable(true);
			modifyFlaggedHillBMDTextField.setDisable(true);
		}
	}

	@Override
	public void handle_FlagHillCheckBox(ActionEvent event)
	{

		if (hillCheckBox.isSelected())
		{
			flagHillkParamComboBox.setDisable(!flagHillkParamCheckBox.isSelected());
			bestModelSeletionWithFlaggedHillLabel.setDisable(!flagHillkParamCheckBox.isSelected());
			bestModelSeletionWithFlaggedHillComboBox.setDisable(!flagHillkParamCheckBox.isSelected());
			modifyFlaggedHillBMDLabel.setDisable(!flagHillkParamCheckBox.isSelected());
			modifyFlaggedHillBMDTextField.setDisable(!flagHillkParamCheckBox.isSelected());

			if (flagHillkParamCheckBox.isSelected())
			{
				setModifyBMDOfFlaggedHillEnabledness();
			}

		}

	}

	@Override
	public void handle_HillCheckBox(ActionEvent event)
	{

		flagHillkParamCheckBox.setDisable(!hillCheckBox.isSelected());
		flagHillkParamComboBox.setDisable(!hillCheckBox.isSelected());
		bestModelSeletionWithFlaggedHillLabel.setDisable(!hillCheckBox.isSelected());
		bestModelSeletionWithFlaggedHillComboBox.setDisable(!hillCheckBox.isSelected());
		modifyFlaggedHillBMDLabel.setDisable(!hillCheckBox.isSelected());
		modifyFlaggedHillBMDTextField.setDisable(!hillCheckBox.isSelected());
		handle_FlagHillCheckBox(event);

	}

	@Override
	public void handle_PowerCheckBox(ActionEvent event)
	{
		restrictPowerComboBox.setDisable(!powerCheckBox.isSelected());
		restrictPowerLabel.setDisable(!powerCheckBox.isSelected());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initData(List<IStatModelProcessable> processableData, boolean selectModelsOnly)
	{
		presenter.initData(processableData);

		this.processableData = processableData;
		if (processableData.size() > 1)
		{
			oneWayANOVADataLabelLabel.setVisible(false);
			expressionDataLabel.setText("Multiple Data Sets");
		}
		else if (processableData.get(0).getParentDataSetName() == null)
		{
			oneWayANOVADataLabelLabel.setVisible(false);
			expressionDataLabel.setText(processableData.toString());
		}
		else
		{
			oneWayANOVADataLabel.setText(processableData.get(0).toString());
			expressionDataLabel.setText(processableData.get(0).getParentDataSetName());
		}

		// init confidence level
		confidenceLevelComboBox.getItems().add("0.95");
		confidenceLevelComboBox.getItems().add("0.99");
		confidenceLevelComboBox.getSelectionModel().select(0);

		// init restrict power
		restrictPowerComboBox.getItems().add("No Restriction");
		restrictPowerComboBox.getItems().add(">=1");
		restrictPowerComboBox.getSelectionModel().select(1);

		// init best poly model test
		bestPolyTestComboBox.getItems().setAll(BestPolyModelTestEnum.values());
		bestPolyTestComboBox.getSelectionModel().select(0);

		// pValue Cut OFF
		pValueCutoffComboBox.getItems().add("0.01");
		pValueCutoffComboBox.getItems().add("0.05");
		pValueCutoffComboBox.getItems().add("0.10");
		pValueCutoffComboBox.getItems().add("0.5");
		pValueCutoffComboBox.getItems().add("1");
		pValueCutoffComboBox.getSelectionModel().select(1);

		flagHillkParamComboBox.getItems().setAll(FlagHillModelDoseEnum.values());

		flagHillkParamComboBox.getSelectionModel().select(FlagHillModelDoseEnum.ONE_THIRD_OF_LOWEST_DOSE);

		bestModelSeletionWithFlaggedHillComboBox.getItems()
				.setAll(BestModelSelectionWithFlaggedHillModelEnum.values());

		bestModelSeletionWithFlaggedHillComboBox.getSelectionModel()
				.select(BestModelSelectionWithFlaggedHillModelEnum.SELECT_NEXT_BEST_PVALUE_GREATER_OO5);

		bestModelSeletionWithFlaggedHillComboBox.valueProperty()
				.addListener(new ChangeListener<BestModelSelectionWithFlaggedHillModelEnum>() {

					@Override
					public void changed(
							ObservableValue<? extends BestModelSelectionWithFlaggedHillModelEnum> observable,
							BestModelSelectionWithFlaggedHillModelEnum oldValue,
							BestModelSelectionWithFlaggedHillModelEnum newValue)
					{
						setModifyBMDOfFlaggedHillEnabledness();
					}

				});

		// let's add 200 threads to drop down
		for (int i = 1; i <= 200; i++)
		{
			numberOfThreadsComboBox.getItems().add(String.valueOf(i));
		}
		numberOfThreadsComboBox.getSelectionModel().select(0);

		// remove most of the panes.
		if (selectModelsOnly)
		{
			mainVBox.getChildren().remove(modelsPane);
			mainVBox.getChildren().remove(parametersPane);
			mainVBox.getChildren().remove(threadPane);
			mainVBox.getChildren().remove(dataOptionsPane);

			this.progressBar.setVisible(false);
			this.progressLabel.setVisible(false);
		}
		this.selectModelsOnly = selectModelsOnly;

		// add data to the bmrFactor combobox
		bMRFactorComboBox.getItems().addAll(initBMRFactors());
		bMRFactorComboBox.getSelectionModel().select(3);

	}

	private ModelInputParameters assignParameters()
	{
		ModelInputParameters inputParameters = new ModelInputParameters();
		if (!selectModelsOnly)
		{
			inputParameters.setIterations(Integer.valueOf(maximumIterationsTextField.getText()));
			inputParameters.setConfidence(Double.valueOf(confidenceLevelComboBox.getEditor().getText()));
			inputParameters.setBmrLevel(Double.valueOf(
					((BMRFactor) bMRFactorComboBox.getSelectionModel().getSelectedItem()).getValue()));
			inputParameters.setNumThreads(Integer.valueOf(numberOfThreadsComboBox.getEditor().getText()));

			inputParameters.setBmdlCalculation(1);
			inputParameters.setBmdCalculation(1);
			inputParameters.setConstantVariance((constantVarianceCheckBox.isSelected()) ? 1 : 0);
			// for simulation only?
			inputParameters.setRestirctPower(restrictPowerComboBox.getSelectionModel().getSelectedIndex());
			// inputParameters.setBMRLevel(1);
			// inputParameters.setObservations(
			// processableData.getProcessableDoseResponseExperiment().getTreatments().size());

			if (inputParameters.getConstantVariance() == 0)
			{
				inputParameters.setRho(inputParameters.getNegative());
			}
		}

		return inputParameters;
	}

	private ModelSelectionParameters assignModelSelectionParameters()
	{
		ModelSelectionParameters modelSelectionParameters = new ModelSelectionParameters();

		modelSelectionParameters.setBestPolyModelTest(
				(BestPolyModelTestEnum) this.bestPolyTestComboBox.getSelectionModel().getSelectedItem());

		// set up the pValue
		modelSelectionParameters.setpValue(Double.valueOf(pValueCutoffComboBox.getEditor().getText()));

		// set up Flag HIll
		modelSelectionParameters.setFlagHillModel(flagHillkParamCheckBox.isSelected());

		modelSelectionParameters.setFlagHillModelDose(
				(FlagHillModelDoseEnum) flagHillkParamComboBox.getSelectionModel().getSelectedItem());

		// best model selection with flagged hill model

		modelSelectionParameters.setBestModelSelectionWithFlaggedHill(
				(BestModelSelectionWithFlaggedHillModelEnum) bestModelSeletionWithFlaggedHillComboBox
						.getSelectionModel().getSelectedItem());

		if (!modifyFlaggedHillBMDTextField.isDisabled())
		{
			modelSelectionParameters.setModFlaggedHillBMDFractionMinBMD(
					Double.valueOf(modifyFlaggedHillBMDTextField.getText()));
		}
		else
		{
			modelSelectionParameters.setModFlaggedHillBMDFractionMinBMD(0.5);
		}

		return modelSelectionParameters;

	}

	@Override
	public void initializeProgressBar(String label)
	{
		progressLabel.setText(label);
		progressBar.setProgress(0.0);

	}

	@Override
	public void updateProgressBar(String label, double value)
	{
		progressLabel.setText(label);
		progressBar.setProgress(value);
	}

	@Override
	public void clearProgressBar()
	{
		progressLabel.setText("");
		progressBar.setProgress(0.0);
		this.startButton.setDisable(false);
		this.progressBar.setVisible(false);

	}

	@Override
	public void finishedBMDAnalysis()
	{
		startButton.setDisable(false);

	}

	@Override
	public void startedBMDAnalysis()
	{
		startButton.setDisable(true);
		startButton.setDisable(true);

	}

	@Override
	public void closeWindow()
	{
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		this.close();
		stage.close();

	}

	@Override
	public void close()
	{
		if (presenter != null)
		{
			presenter.close();
		}

	}

	private List<BMRFactor> initBMRFactors()
	{
		List<BMRFactor> factors = new ArrayList<>();
		factors.add(new BMRFactor("0.522 (1%)", "0.522"));
		factors.add(new BMRFactor("1 SD", "1.0"));
		factors.add(new BMRFactor("1.021 (5%)", "1.021"));
		factors.add(new BMRFactor("1.349 (10%)", "1.349"));
		factors.add(new BMRFactor("1.581 (15%)", "1.581"));
		factors.add(new BMRFactor("1.932484 (25%)", "1.932484"));
		factors.add(new BMRFactor("2 SD", "2.0"));
		factors.add(new BMRFactor("2.600898 (50%)", "2.600898"));
		factors.add(new BMRFactor("2.855148 (60%)", "2.855148"));
		factors.add(new BMRFactor("3 SD", "3.0"));
		return factors;
	}

	/*
	 * store the BMR Factors here.
	 */
	private class BMRFactor
	{
		private String description;
		private String value;

		public BMRFactor(String d, String v)
		{
			description = d;
			value = v;
		}

		@Override
		public String toString()
		{
			return description;
		}

		public String getValue()
		{
			return value;
		}
	}

}
