import javax.swing.table.*;
import java.sql.*;
import java.util.*;
/* TableModel inmutable construido 
  de los metadatos de una tabla en una BD JDBC 
 */
public final class JDBCTableModel extends AbstractTableModel {
    Object[][] contents;
    ArrayList<String> columnNames;
    ArrayList<Class> columnClasses;
    /* El constructor por defecto requiere conexión y nombre de tabla */
    public JDBCTableModel(Connection conn,
            String tableName)
            throws SQLException {
        super();
        getTableContents(conn, tableName);
    }
    /* Método de obtención de contenidos de tabla */
    protected void getTableContents(Connection conn,
            String tableName)
            throws SQLException {
        /* getMetaData: qué columnas existen y
         de qué tipo (clase) son */
        DatabaseMetaData meta = conn.getMetaData();
        System.out.println("Metadatos obtenidos = " + meta);
        ResultSet results
                = meta.getColumns(null, null, tableName, null);
        System.out.println("Obtenidos resultados de columna");
        columnNames = new ArrayList<>();
        columnClasses = new ArrayList<>();
        while (results.next()) {
            columnNames.add(results.getString("COLUMN_NAME"));
            System.out.println("Nombre: "
                    + results.getString("COLUMN_NAME"));
            int dbType = results.getInt("DATA_TYPE");
            /* Nuevo formato de switch desde JDK 13 */
            switch (dbType) {
                case Types.INTEGER ->
                    columnClasses.add(Integer.class);
                case Types.FLOAT ->
                    columnClasses.add(Float.class);
                case Types.DOUBLE, Types.REAL ->
                    columnClasses.add(Double.class);
                case Types.DATE, Types.TIME, Types.TIMESTAMP ->
                    columnClasses.add(java.sql.Date.class);
                default ->
                    columnClasses.add(String.class);
            }
            System.out.println("Tipo: "
                    + results.getInt("DATA_TYPE"));
        }
        /* Obtiene todos los datos de la tabla
         y los coloca en un ArrayList de contenidos */
        Statement statement = conn.createStatement();
        results = statement.executeQuery("SELECT * FROM " + tableName);
        ArrayList rowList = new ArrayList();
        while (results.next()) {
            ArrayList cellList = new ArrayList();
            for (int i = 0; i < columnClasses.size(); i++) {
                Object cellValue = null;
                if (columnClasses.get(i) == String.class) {
                    cellValue = results.getString(columnNames.get(i));
                } else if (columnClasses.get(i) == Integer.class) {
                    cellValue = results.getInt(columnNames.get(i));
                } else if (columnClasses.get(i) == Float.class) {
                    cellValue = results.getFloat(columnNames.get(i));
                } else if (columnClasses.get(i) == Double.class) {
                    cellValue = results.getDouble(columnNames.get(i));
                } else if (columnClasses.get(i) == java.sql.Date.class) {
                    cellValue = results.getDate(columnNames.get(i));
                } else {
                    System.out.println("No puedo asignar " + columnNames.get(i));
                }
                cellList.add(cellValue);
            }
            Object[] cells = cellList.toArray();
            rowList.add(cells);
        }
        /* Finalmente se crea el array bidimensional de contenidos */
        contents = new Object[rowList.size()][];
        for (int i = 0; i < contents.length; i++) {
            contents[i] = (Object[]) rowList.get(i);
        }
        System.out.println("Modelo creado con "
                + contents.length + " filas");
        results.close();
        statement.close();
    }
    /* Métodos de AbstractTableModel */
    @Override
    public int getRowCount() {
        return contents.length;
    }
    @Override
    public int getColumnCount() {
        if (contents.length == 0) {
            return 0;
        } else {
            return contents[0].length;
        }
    }
    @Override
    public Object getValueAt(int row, int column) {
        return contents[row][column];
    }
    /* Métodos de sobrecarga para los cuales AbstractTableModel
    tiene implementaciones */
    @Override
    public Class getColumnClass(int col) {
        return columnClasses.get(col);
    }
    @Override
    public String getColumnName(int col) {
        return columnNames.get(col);
    }
}
