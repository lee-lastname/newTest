/**
 * create date: Aug 11, 2008
 */
package com.isoftstone.pcis.policy.dm.bo;

 
/**
 * @author jiangqf
 *
 */
public class EndorsementApplication  extends AbstractEndorsement{

	@Override
	public PolicyApplication getNewPolicy() {
		return (PolicyApplication)super.getNewPolicy();
	}

}
