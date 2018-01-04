package com.isoftstone.pcis.policy.dm.bo;

import com.isoftstone.pcis.policy.vo.AppBaseVO;

public class PolicyApplication extends AbstractPolicy{
	private static final long serialVersionUID = -8267757488595856290L;
	
    @Override
	public AppBaseVO getBase() {
		return (AppBaseVO)super.getBase();
	}
    
	public void setStatus(String appStatus){
    	((AppBaseVO)this.getBase()).setCAppStatus(appStatus);
    }
    public void setUndrMrk(String mark){
        this.getBase().setCUdrMrk(mark);
    }

	@Override
	public void setDefaultValue() {
		//加载抽象类方法
		super.setDefaultValue();
	}
    
}
