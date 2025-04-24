package assessor.component.chart.renderer;

import org.jfree.chart.renderer.xy.ClusteredXYBarRenderer;
import assessor.component.chart.themes.DefaultChartTheme;

public class ChartXYBarRenderer extends ClusteredXYBarRenderer {

    public ChartXYBarRenderer() {
        setBarPainter(DefaultChartTheme.getInstance().getXYBarPainter());
        setShadowVisible(DefaultChartTheme.getInstance().isShadowVisible());
        setMargin(0.3);
    }

    @Override
    public String toString() {
        return "Bar";
    }
}
