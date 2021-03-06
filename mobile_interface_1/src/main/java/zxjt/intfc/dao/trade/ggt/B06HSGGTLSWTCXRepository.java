package zxjt.intfc.dao.trade.ggt;

import java.util.List;

import org.springframework.stereotype.Repository;

import zxjt.intfc.dao.common.BaseRepository;
import zxjt.intfc.entity.trade.ggt.B06HSGGTLSWTCX;

@Repository
	public interface B06HSGGTLSWTCXRepository extends BaseRepository<B06HSGGTLSWTCX> {

	//查询
	public List<B06HSGGTLSWTCX> findByFunctionidAndIsExcuteIgnoreCase(int functionid,String isExcute);	
}
