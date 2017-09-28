package ganguo.oven.db;

import java.util.List;

import org.orman.mapper.C;
import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;
import org.orman.sql.Criteria;
import org.orman.sql.Criterion;
import org.orman.sql.Query;


/**
 * @ClassName PointDao.java
 * @Description 消息查询
 * @author Jeff feng jfeng@xtremeprog.com
 * @date 2015-7-2
 */
public class PointDao {
	
	private static PointDao pointDao;
	
	public static PointDao getInstance(){
		if(pointDao == null){
			synchronized (PointDao.class) {
				if(pointDao == null){
					pointDao = new PointDao();
				}
			}
		}
		return pointDao;
	}
	
	private PointDao(){
	}
	
	/**
	 * 通过date查找所有点集合
	 */                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
	public List<Point> getPointsByDate(String myDate){
		Criterion limit = C.eq(Point.class, "myDate", myDate);
		List<Point> result  = sqlProcess(limit);
		return result;
	}
	
	/**
	 * 通过myDate和x查找一个point
	 */
	public Point findPointByDateAndX(String myDate,int x){
		Criteria limit = C.and(C.eq(Point.class, "myDate", myDate),C.eq(Point.class, "x", x));
		Point result = sqlOneProcess(limit);
		return result;
	}
	
	/**
	 * 通过x查找一个point
	 */
	public Point findPointByX(int x){
		Criterion limit = C.eq(Point.class, "x", x);
		Point result = sqlOneProcess(limit);
		return result;
	}
	
	/**
	 * 查找批量数据
	 */
	private List<Point> sqlProcess(Criterion limit) {
		Query query = ModelQuery.select().from(Point.class).where(limit).getQuery();
		List<Point> result = Model.fetchQuery(query, Point.class);
		return result;
	}
	/**
	 * 查找单个数据
	 * @param limit
	 * @return
	 */
	private Point sqlOneProcess(Criterion limit) {
		Query query = ModelQuery.select().from(Point.class).where(limit).getQuery();
		Point Dish = Model.fetchSingle(query, Point.class);
		return Dish;
	}
	
}
