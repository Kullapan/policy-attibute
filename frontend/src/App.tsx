import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import DictionaryPage from './pages/DictionaryPage';
import PolicyMappingPage from './pages/PolicyMappingPage';
import BulkUploadPage from './pages/BulkUploadPage';
import PolicyListPage from './pages/PolicyListPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<DictionaryPage />} />
          <Route path="/policies" element={<PolicyListPage />} />
          <Route path="/policy-mapping" element={<PolicyMappingPage />} />
          <Route path="/bulk-upload" element={<BulkUploadPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
