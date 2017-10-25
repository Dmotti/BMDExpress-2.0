package com.sciome.bmdexpress2.mvp.model.prefilter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sciome.bmdexpress2.mvp.model.BMDExpressAnalysisDataSet;
import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpress2.mvp.model.IStatModelProcessable;
import com.sciome.bmdexpress2.mvp.model.LogTransformationEnum;
import com.sciome.bmdexpress2.mvp.model.info.AnalysisInfo;
import com.sciome.bmdexpress2.mvp.model.probe.ProbeResponse;
import com.sciome.bmdexpress2.mvp.model.refgene.ReferenceGeneAnnotation;
import com.sciome.charts.annotation.ChartableData;
import com.sciome.charts.annotation.ChartableDataLabel;

public class OriogenResults extends BMDExpressAnalysisDataSet implements Serializable, IStatModelProcessable, PrefilterResults {
	/**
	 * 
	 */
	private static final long serialVersionUID = 343075642510090330L;

	private String						name;

	private DoseResponseExperiment		doseResponseExperiment;
	private List<OriogenResult>			oriogenResults;
	private AnalysisInfo				analysisInfo;
	private transient List<String>		columnHeader;

	private Long						id;

	/* define chartabble key values */
	public static final String			UNADJUSTED_PVALUE			= "Unadjusted P-Value";
	public static final String			ADJUSTED_PVALUE				= "Adjusted P-Value";
	public static final String			NEG_LOG_ADJUSTED_PVALUE		= "Negative Log 10 Adjusted P-Value";
	public static final String			BEST_FOLD_CHANGE			= "Max Fold Change";
	public static final String			BEST_FOLD_CHANGE_ABS		= "Max Fold Change Unsigned";
	public static final String			FOLD_CHANGE					= "Fold Change";
	public static final String			GENE_ID						= "Gene ID";
	public static final String			GENE_SYMBOL					= "Gene Symbol";
	public static final String			PROBE_ID					= "Probe ID";
	public static final String			PROFILE						= "Profile";

	public static final String			NEG_LOG_UNADJUSTED_PVALUE	= "Negative Log 10 Unadjusted P-Value";

	@JsonIgnore
	public Long getID()
	{
		return id;
	}

	public void setID(Long id)
	{
		this.id = id;
	}

	@ChartableData(key = "Oriogen")
	public List<OriogenResult> getOriogenResults()
	{
		return oriogenResults;
	}

	public void setOriogenResults(List<OriogenResult> OriogenResults)
	{
		this.oriogenResults = OriogenResults;
	}

	@ChartableDataLabel(key = "Oriogen")
	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public DoseResponseExperiment getDoseResponseExperiement()
	{
		return doseResponseExperiment;
	}

	public void setDoseResponseExperiement(DoseResponseExperiment doseResponseExperiement)
	{
		this.doseResponseExperiment = doseResponseExperiement;
	}

	@Override
	public String toString()
	{
		return name;
	}

	@JsonIgnore
	public List<ProbeResponse> getProbeResponses()
	{
		List<ProbeResponse> probeResponses = new ArrayList<>();

		if (oriogenResults != null)
		{
			for (OriogenResult williamsResult : oriogenResults)
			{
				probeResponses.add(williamsResult.getProbeResponse());
			}
		}

		return probeResponses;
	}

	/*
	 * Implement the IStatModelProcessable methods.
	 */
	@Override
	@JsonIgnore
	public DoseResponseExperiment getProcessableDoseResponseExperiment()
	{
		return this.doseResponseExperiment;
	}

	@Override
	@JsonIgnore
	public List<ProbeResponse> getProcessableProbeResponses()
	{
		return this.getProbeResponses();
	}

	@Override
	@JsonIgnore
	public String getParentDataSetName()
	{
		return this.doseResponseExperiment.toString();
	}

	@Override
	public AnalysisInfo getAnalysisInfo()
	{
		return analysisInfo;
	}

	public void setAnalysisInfo(AnalysisInfo analysisInfo)
	{
		this.analysisInfo = analysisInfo;
	}

	/*
	 * fill the column header for table display or file export purposes.
	 */
	private void fillColumnHeader()
	{
		columnHeader = new ArrayList<>();
		if (oriogenResults == null || oriogenResults.size() == 0)
		{
			return;
		}

		columnHeader.add("Probe ID");

		columnHeader.add("Genes");

		columnHeader.add("Gene Symbols");

		// p value
		columnHeader.add("P-Value");

		// adjusted p value
		columnHeader.add("Adjusted P-Value");

		if (oriogenResults.get(0).getBestFoldChange() != null)
		{
			columnHeader.add("Max Fold Change Value");
		}

		if (oriogenResults.get(0).getFoldChanges() != null)
		{
			int i = 1;
			for (Float foldChange : oriogenResults.get(0).getFoldChanges())
			{
				columnHeader.add("FC Dose Level " + i);
				i++;
			}
		}

		columnHeader.add("Profile");
	}

	@Override
	@JsonIgnore
	public List<String> getColumnHeader()
	{
		if (columnHeader == null || columnHeader.size() == 0)
		{
			fillColumnHeader();
			fillRowData();
		}
		return columnHeader;
	}

	// This is called in order to generate data for each probe stat result for viewing
	// data in a table or exporting it.
	private void fillRowData()
	{
		// This will allow gene/gene symbol lists to be come part of the table.
		Map<String, ReferenceGeneAnnotation> probeToGeneMap = new HashMap<>();

		if (this.doseResponseExperiment.getReferenceGeneAnnotations() != null)
		{
			for (ReferenceGeneAnnotation refGeneAnnotation : this.doseResponseExperiment
					.getReferenceGeneAnnotations())
			{
				probeToGeneMap.put(refGeneAnnotation.getProbe().getId(), refGeneAnnotation);
			}
		}
		for (OriogenResult oriogenResult : oriogenResults)
		{
			oriogenResult.createRowData(probeToGeneMap);
		}
	}

	@Override
	@JsonIgnore
	public List getAnalysisRows()
	{
		return oriogenResults;
	}

	@Override
	@JsonIgnore
	public List<Object> getColumnHeader2()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<PrefilterResult> getPrefilterResults() {
		return (List<PrefilterResult>) (List<?>) oriogenResults;
	}

	@Override
	@JsonIgnore
	public LogTransformationEnum getLogTransformation() {
		return this.getDoseResponseExperiement().getLogTransformation();
	}
}