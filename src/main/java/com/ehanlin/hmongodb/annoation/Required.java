package com.ehanlin.hmongodb.annoation;

/**
 * <p>有加入 @Required 的欄位，會自動在查詢時於查詢式不包括這個欄位，則自動加入 {$exists: true}。</p>
 * <p>這個欄位還有另一種功能，若不含 @Required 的值在轉換時為 null，則不傳換這個欄位。<br/>
 * 若欄位有 @Required 則把這個欄位以 null 的值寫入。</p>
 * <p>例如：<br/>
 * class model1 {<br/>
 *     @Required<br/>
 *     public String p1 = null;<br/>
 *     public String p2 = null;<br/>
 *     public String p3 = "abc";<br/>
 * }<br/>
 * 轉換後會產生以下的 DBObject<br/>
 * {"p1":null, "p3":"abc"}</p>
 */
public @interface Required {

}
