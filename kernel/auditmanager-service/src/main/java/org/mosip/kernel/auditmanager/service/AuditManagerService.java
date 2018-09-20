/**
 * 
 */
package org.mosip.kernel.auditmanager.service;

import org.mosip.kernel.auditmanager.dto.AuditResponseDto;
import org.mosip.kernel.auditmanager.model.Audit;
import org.mosip.kernel.auditmanager.request.AuditRequestDto;

/**
 * Interface for AuditManager Serivce having function to add new {@link Audit}
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
public interface AuditManagerService {

	/**
	 * Function to add new audit
	 * 
	 * @param auditRequestDto
	 *            The {@link AuditRequestDto} having required field to audit
	 * @return The {@link AuditResponseDto} having status of audit
	 */
	AuditResponseDto addAudit(AuditRequestDto auditRequestDto);

}