package io.github.sinri.keel.servant.intravenous;

/**
 * 小任务，相当于静脉滴注的一滴。
 * 仅需在此定义各类源数据，相当于点滴中的有效成分。
 *
 * @since 2.7
 */
public interface KeelIntravenousDrop {

    String getReference();

}
