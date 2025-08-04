package im.geth.mrshelper

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import java.awt.GridLayout
import javax.swing.*

class TemplateToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(buildUI(project), "", false)
        toolWindow.contentManager.addContent(content)
    }

    private fun buildUI(project: Project): JBScrollPane {
        val root = JPanel()
        root.layout = BoxLayout(root, BoxLayout.Y_AXIS)

        val groups = TemplateLoader.loadMetadata()
        for ((_, groupData) in groups) {
            val panel = JPanel(GridLayout(0, 2, 4, 4))
            panel.border = BorderFactory.createTitledBorder(groupData.label)

            for (template in groupData.templates) {
                val button = JButton(template.name)
                button.toolTipText = template.description
                button.addActionListener {
                    TemplateAction().insertTemplate(project, template.file)
                }
                panel.add(button)
            }

            root.add(panel)
            root.add(Box.createVerticalStrut(10))
        }

        return JBScrollPane(root)
    }
}
