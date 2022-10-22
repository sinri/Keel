package io.github.sinri.keel.servant.intravenous.legacy;

/**
 * 小任务，相当于静脉滴注的一滴。
 * 仅需在此定义各类源数据，相当于点滴中的有效成分。
 *
 * @since 2.7
 */
@Deprecated(since = "2.9", forRemoval = true)
public interface KeelIntravenousDrop {

    String getReference();

}
