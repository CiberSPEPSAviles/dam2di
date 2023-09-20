import javax.swing.table.*; 
import java.sql.*; 
import java.util.*;
/* TableModel inmutable construido 
  de los metadatos de una tabla en una BD JDBC 
*/ 
public class JDBCTableModel extends AbstractTableModel {
        Object[][] contents;
        String[] columnNames;
        Class[] columnClasses;
    // El constructor por defecto requiere conexión y nombre de tabla
    public JDBCTableModel (Connection conn,
                       String tableName)
            throws SQLException {
            super();
            getTableContents (conn, tableName);
    }
    // Método de obtención de contenidos de tabla
    protected void getTableContents (Connection conn,
                                 String tableName)
                throws SQLException {
        // getMetaData: qué columnas existen y
        // de qué tipo (clase) son
        DatabaseMetaData meta = conn.getMetaData();
        System.out.println ("Metadatos obtenidos = " + meta);
        ResultSet results =
                meta.getColumns (null, null, tableName, null) ;
        System.out.println ("Obtenidos resultados de columna");
        ArrayList colNamesList = new ArrayList();
        ArrayList colClassesList = new ArrayList();
        while (results.next()) {
            colNamesList.add (results.getString ("COLUMN_NAME")); 
            System.out.println ("Nombre: " + 
                    results.getString ("COLUMN_NAME"));
            int dbType = results.getInt ("DATA_TYPE");
            switch (dbType) {
                case Types.INTEGER:
                    colClassesList.add (Integer.class); 
                    break; 
                case Types.FLOAT:
                    colClassesList.add (Float.class); 
                    break; 
                case Types.DOUBLE: 
                case Types.REAL:
                    colClassesList.add (Double.class); break; 
                case Types.DATE: 
                case Types.TIME: 
                case Types.TIMESTAMP:
                    colClassesList.add (java.sql.Date.class); break; 
                default:
                    colClassesList.add (String.class); break; 
            }; 
            System.out.println ("Tipo: " +
                    results.getInt ("DATA_TYPE"));
        }
        columnNames = new String [colNamesList.size()];
        colNamesList.toArray (columnNames);
        columnClasses = new Class [colClassesList.size()];
        colClassesList.toArray (columnClasses);
        // Obtiene todos los datos de la tabla
        // y los coloca en un array de contenidos
        Statement statement = conn.createStatement ();
        results = statement.executeQuery ("SELECT * FROM " +tableName);
        ArrayList rowList = new ArrayList();
        while (results.next()) {
            ArrayList cellList = new ArrayList(); 
            for (int i = 0; i<columnClasses.length; i++) { 
                Object cellValue = null;
                if (columnClasses[i] == String.class) 
                    cellValue = results.getString (columnNames[i]); 
                else if (columnClasses[i] == Integer.class) 
                    cellValue = Integer.valueOf(results.getInt (columnNames[i])); 
                else if (columnClasses[i] == Float.class) 
                    cellValue = Float.valueOf(results.getInt (columnNames[i])); 
                else if (columnClasses[i] == Double.class) 
                    cellValue = Double.valueOf(results.getDouble (columnNames[i]));
                else if (columnClasses[i] == java.sql.Date.class) 
                    cellValue = results.getDate (columnNames[i]); 
                else 
                    System.out.println ("No puedo asignar " + columnNames[i]);
                cellList.add (cellValue);
            }
            Object[] cells = cellList.toArray();
            rowList.add (cells);
        }
        // Finalmente se crea el array bidimensional de contenidos
        contents = new Object[rowList.size()] [];
        for (int i=0; i<contents.length; i++)
            contents[i] = (Object []) rowList.get (i);
        System.out.println ("Modelo creado con " +
            contents.length + " filas");
        results.close();
        statement.close();
    }
    // Métodos de AbstractTableModel
    public int getRowCount() {
        return contents.length;
    }
    public int getColumnCount() {
        if (contents.length == 0)
            return 0;
        else
            return contents[0].length;
    }
    public Object getValueAt (int row, int column) {
        return contents [row][column];
    }

    // Métodos de sobrecarga para los cuales AbstractTableModel
    // tiene implementaciones
    public Class getColumnClass (int col) {
        return columnClasses [col];
    }
    public String getColumnName (int col) { 
        return columnNames [col]; 
    } 
}
