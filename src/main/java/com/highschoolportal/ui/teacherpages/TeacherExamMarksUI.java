package com.highschoolportal.ui.teacherpages;

import com.highschoolportal.app.App;
import com.highschoolportal.model.User;
import com.highschoolportal.model.ExamMarkRules;
import com.highschoolportal.repository.ExamMarkRepository.MarkEntry;
import com.highschoolportal.services.ExamMarkService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** UR-F32: enter and revise exam series marks for assigned students. */
public final class TeacherExamMarksUI extends JDialog {
    private final App app;
    private final User user;
    private final ExamMarkService service = new ExamMarkService();
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Student","Subject","Academic year","Series entered","Final mark","Result","Marking"},0) {
        @Override public boolean isCellEditable(int row,int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JSpinner series = new JSpinner(new SpinnerNumberModel(1,1,1,1));
    private final JTextField mark = new JTextField(8);
    private final JButton save = new JButton("Save mark"), refresh = new JButton("Refresh"), close = new JButton("Close");
    private final JLabel info = new JLabel("Select a student and examination series."), status = new JLabel(" ");
    private List<MarkEntry> entries = List.of();
    private boolean busy;

    public static void open(App app) {
        User user=app.getCurrentUser();
        if(user==null) return;
        TeacherExamMarksUI dialog=new TeacherExamMarksUI(app,user);
        dialog.load(-1,1);
        dialog.setVisible(true);
    }
    private TeacherExamMarksUI(App app,User user) {
        super(app.getCurrentFrame(),"Teacher exam marks",true);
        this.app=app;this.user=user;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        JPanel panel=new JPanel(new BorderLayout(10,10));
        panel.setBorder(new EmptyBorder(16,16,16,16));
        panel.add(new JLabel("Exam marks for assigned subjects"),BorderLayout.NORTH);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        panel.add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel footer=new JPanel(new BorderLayout(6,6));
        JPanel controls=new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Series:"));controls.add(series);
        controls.add(new JLabel("Mark:"));controls.add(mark);controls.add(save);controls.add(refresh);controls.add(close);
        footer.add(info,BorderLayout.NORTH);footer.add(controls,BorderLayout.CENTER);footer.add(status,BorderLayout.SOUTH);
        panel.add(footer,BorderLayout.SOUTH);setContentPane(panel);
        setSize(940,480);setLocationRelativeTo(app.getCurrentFrame());
        table.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting()) selectionChanged();});
        series.addChangeListener(e->showMark());
        save.addActionListener(e->save());
        refresh.addActionListener(e->load(-1,1));
        close.addActionListener(e->{if(!busy) dispose();});
        addWindowListener(new WindowAdapter(){@Override public void windowClosing(WindowEvent e){if(!busy)dispose();}});
        updateControls();
    }
    private MarkEntry selected() {
        int row=table.getSelectedRow();
        if(row<0)return null;
        int index=table.convertRowIndexToModel(row);
        return index>=0&&index<entries.size()?entries.get(index):null;
    }
    private void selectionChanged() {
        MarkEntry entry=selected();
        series.setModel(new SpinnerNumberModel(1,1,entry==null?1:Math.max(1,entry.seriesCount()),1));
        if(series.getEditor() instanceof JSpinner.DefaultEditor editor) editor.getTextField().setEditable(false);
        showMark();updateControls();
    }
    private void showMark() {
        MarkEntry entry=selected();
        var current=entry==null?null:entry.marks().get((Integer)series.getValue());
        mark.setText(current==null?"":current.toPlainString());
        if(entry==null)info.setText("Select a student and examination series.");
        else info.setText("Pass mark: "+entry.passMark()+" | Subject maximum (all series): "+entry.maxMark()+" | Existing marks: "+entry.marks());
    }
    private boolean current() {return app.getCurrentUser()==user;}
    private void updateControls() {
        MarkEntry entry=selected();boolean editable=!busy&&current()&&entry!=null&&entry.editable();
        save.setEnabled(editable);mark.setEnabled(editable);series.setEnabled(!busy&&entry!=null&&entry.seriesCount()>0);
        refresh.setEnabled(!busy&&current());close.setEnabled(!busy);table.setEnabled(!busy);
    }
    private String marking(MarkEntry entry) {
        if(entry.frozen())return "Frozen";
        if(!entry.examOpen())return "Exam closed";
        return entry.editable()?"Open":"Rules missing";
    }
    private void load(int detailID,int selectedSeries) {
        if(busy||!current())return;
        busy=true;updateControls();status.setText("Loading exam marks...");
        new SwingWorker<List<MarkEntry>,Void>() {
            @Override protected List<MarkEntry> doInBackground() throws Exception {return service.listForTeacher(user);}
            @Override protected void done() {
                busy=false;
                if(!current()){dispose();return;}
                try {
                    table.clearSelection();model.setRowCount(0);entries=get();
                    int restore=-1;
                    for(int i=0;i<entries.size();i++){
                        MarkEntry e=entries.get(i);
                        model.addRow(new Object[]{e.student(),e.subject(),e.academicYear(),e.marks().size()+" / "+e.seriesCount(),e.finalMark(),e.resultStatus(),marking(e)});
                        if(e.detailID()==detailID)restore=i;
                    }
                    if(restore>=0){int view=table.convertRowIndexToView(restore);table.setRowSelectionInterval(view,view);if(selectedSeries<=entries.get(restore).seriesCount())series.setValue(selectedSeries);}
                    status.setText(entries.isEmpty()?"No assigned student enrolments found.":"Loaded "+entries.size()+" enrolment(s).");
                }catch(Exception ex){entries=List.of();model.setRowCount(0);status.setText("Unable to load marks.");showError(ex);}
                updateControls();
            }
        }.execute();
    }
    private void save() {
        MarkEntry entry=selected();if(busy||!current()||entry==null||!entry.editable())return;
        try {series.commitEdit();}catch(java.text.ParseException ex){JOptionPane.showMessageDialog(this,"Select a valid series.");return;}
        int number=(Integer)series.getValue();String value=mark.getText();
        busy=true;updateControls();status.setText("Saving mark...");
        new SwingWorker<ExamMarkRules.Result,Void>() {
            @Override protected ExamMarkRules.Result doInBackground() throws Exception {return service.saveMark(user,entry.detailID(),number,value);}
            @Override protected void done(){
                busy=false;if(!current()){dispose();return;}
                try {ExamMarkRules.Result result=get();JOptionPane.showMessageDialog(TeacherExamMarksUI.this,"Mark saved. Total: "+result.total()+" | Result: "+result.status());load(entry.detailID(),number);}
                catch(Exception ex){status.setText("Mark was not saved.");showError(ex);updateControls();}
            }
        }.execute();
    }
    private void showError(Exception error) {
        Throwable cause=error instanceof ExecutionException&&error.getCause()!=null?error.getCause():error;
        JOptionPane.showMessageDialog(this,cause.getMessage(),"Exam marks",JOptionPane.ERROR_MESSAGE);
    }
}
