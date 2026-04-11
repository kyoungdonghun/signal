import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { BriefingPage } from './pages/BriefingPage';
import { TrackRecordPage } from './pages/TrackRecordPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<BriefingPage />} />
          <Route path="/track-record" element={<TrackRecordPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
