package aeminium.gpu.backends.gpu.generators;

import java.util.ArrayList;

import aeminium.gpu.backends.gpu.buffers.OtherData;

public abstract class AbstractCodeGen {
	protected ArrayList<OtherData> otherData;
	protected String otherSources = "";
	protected boolean isRange = false;
	protected String id;
	
	public void setOtherData(ArrayList<OtherData> ods) {
		this.otherData = ods;
	}
	
	public boolean isRange() {
		return isRange;
	}

	public void setRange(boolean isRange) {
		this.isRange = isRange;
	}
	
	public String getExtraArgs() {
		if (otherData == null || otherData.size() == 0) {
			return "";
		} else {
			StringBuilder b = new StringBuilder();
			for (OtherData o : otherData) {
				if (o.obj.isNative()) {
					b.append(", " + o.type + " " + o.name);
				} else {
					b.append(", __global " + o.type + " " + o.name);
				}
			}
			return b.toString();
		}
	}
	
	public String getExtraArgsCall() {
		if (otherData == null || otherData.size() == 0) {
			return "";
		} else {
			StringBuilder b = new StringBuilder();
			for (OtherData o : otherData) {
				b.append(", " + o.name);
			}
			return b.toString();
		}
	}
}
