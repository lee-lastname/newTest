package com.isoftstone.pcis.policy.dm.bo;

import com.isoftstone.pcis.policy.core.exception.VoManipulateException;
import com.isoftstone.pcis.policy.vo.PlyBaseVO;

public class Policy extends AbstractPolicy {
    /**
	 *
	 */
	private static final long serialVersionUID = -2011988582247765476L;

	@Override
	public PlyBaseVO getBase() {
		return (PlyBaseVO)super.getBase();
	}

	public String getPlyNo(){
        return this.getBase().getCPlyNo();
    }

    public boolean isLatest(){
        return ((PlyBaseVO)getBase()).getCLatestMrk().equals("1")?true:false;
    }

    public void setNEdrPrjNo(Long NEdrPrjNo) throws VoManipulateException{
        this.setPropertyForAllEntries("NEdrPrjNo", NEdrPrjNo,true);
    }
    
    @Override
	public void setDefaultValue() {
		//加载抽象类方法
		super.setDefaultValue();
	}
}
